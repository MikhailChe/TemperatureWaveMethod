package thermophysics.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import controller.experiment.analyzer.MeasurementCalculator;
import model.measurement.Diffusivity;
import model.signalID.BaseSignalID;
import model.signalID.SignalIdentifier;
import tools.jackson.databind.json.JsonMapper;

@Service
class CalculationWorker {
    private record PendingRun(UUID id, UUID experimentId, UUID calibrationId) {}
    private record ChannelResult(int column, double amplitude, double phaseRadians, double kappa,
                                 double diffusivity, double capacitance) {
        static ChannelResult from(Diffusivity value) {
            if (!Double.isFinite(value.diffusivity) || !Double.isFinite(value.capacitance)) {
                throw new IllegalArgumentException("Non-finite result on channel " + value.channelNumber);
            }
            return new ChannelResult(value.channelNumber, value.amplitude, value.phase, value.kappa,
                    value.diffusivity, value.capacitance);
        }
    }
    private record Result(double frequencyHz, List<ChannelResult> channels) {}

    private final JdbcClient jdbc;
    private final Laboratory laboratory;
    private final JsonMapper json;

    CalculationWorker(JdbcClient jdbc, Laboratory laboratory, JsonMapper json) {
        this.jdbc = jdbc;
        this.laboratory = laboratory;
        this.json = json;
    }

    @Scheduled(fixedDelayString = "${twm.calculation.poll-delay-ms:500}")
    @Transactional
    public void processNext() {
        jdbc.sql("""
                select id, experiment_id, calibration_id from calculation_runs
                where status = 'QUEUED' order by requested_at, id limit 1 for update skip locked
                """).query(PendingRun.class).optional().ifPresent(this::calculate);
    }

    private void calculate(PendingRun run) {
        var experiment = laboratory.experiment(run.experimentId());
        int lastColumn = experiment.channels().stream().mapToInt(Laboratory.Channel::column).max().orElseThrow();
        List<SignalIdentifier> channels;
        try {
            channels = loadChannels(run.calibrationId(), experiment.channels(), lastColumn);
        } catch (IOException e) {
            jdbc.sql("update run_measurements set error_message = ? where run_id = ?")
                    .params("Could not load stored calibration", run.id()).update();
            setStatus(run.id(), "FAILED");
            return;
        }

        var properties = new MeasurementCalculator.SampleProperties(experiment.lengthMeters(), experiment.density());
        var calculator = new MeasurementCalculator(properties, channels);
        List<Laboratory.MeasurementFile> files = jdbc.sql("""
                select m.id, m.experiment_id, m.measured_at, m.file_name
                from run_measurements rm join measurements m on m.id = rm.measurement_id
                where rm.run_id = ? order by m.measured_at, m.id
                """).param(run.id()).query(Laboratory.MeasurementFile.class).list();

        int successful = 0;
        for (var file : files) {
            byte[] contents = jdbc.sql("select contents from measurements where id = ?").param(file.id())
                    .query((rs, row) -> rs.getBytes(1)).single();
            String serializedResult;
            try {
                var reader = CalculationFiles.loadMeasurement(contents, file.measuredAt());
                if (reader.getColumnCount() <= lastColumn) {
                    throw new IllegalArgumentException("Measurement is missing requested channel " + lastColumn);
                }
                if (reader.getExperimentFrequency() <= 0) {
                    throw new IllegalArgumentException("Measurement frequency must be positive");
                }
                var measurement = calculator.calculate(reader);
                var result = new Result(measurement.frequency, measurement.diffusivity.stream().map(ChannelResult::from).toList());
                serializedResult = json.writeValueAsString(result);
            } catch (IOException | IllegalArgumentException e) {
                jdbc.sql("update run_measurements set error_message = ? where run_id = ? and measurement_id = ?")
                        .params(e.getMessage(), run.id(), file.id()).update();
                continue;
            }
            jdbc.sql("update run_measurements set result = ? where run_id = ? and measurement_id = ?")
                    .params(serializedResult, run.id(), file.id()).update();
            successful++;
        }
        setStatus(run.id(), successful == files.size() ? "COMPLETED" : successful == 0 ? "FAILED" : "PARTIAL");
    }

    private List<SignalIdentifier> loadChannels(UUID calibrationId, List<Laboratory.Channel> configured, int lastColumn)
            throws IOException {
        Locale locale = Locale.forLanguageTag(jdbc.sql("select number_locale from calibrations where id = ?")
                .param(calibrationId).query(String.class).single());
        List<SignalIdentifier> channels = new ArrayList<>(Collections.nCopies(lastColumn + 1, null));
        for (Laboratory.Channel channel : configured) {
            byte[] contents = jdbc.sql("select contents from calibration_files where calibration_id = ? and channel = ?")
                    .params(calibrationId, channel.column()).query((rs, row) -> rs.getBytes(1)).single();
            var id = new BaseSignalID();
            id.inverse = channel.inverse();
            id.phaseAdjust = CalculationFiles.loadCalibration(contents, locale);
            channels.set(channel.column(), id);
        }
        return channels;
    }

    private void setStatus(UUID runId, String status) {
        jdbc.sql("update calculation_runs set status = ? where id = ?").params(status, runId).update();
    }
}
