package thermophysics.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import controller.experiment.reader.ExperimentFileReader;
import model.phaseAdjust.PhaseAdjust;

final class CalculationFiles {
    private CalculationFiles() {}

    static PhaseAdjust loadCalibration(byte[] contents, Locale locale) throws IOException {
        var file = Files.createTempFile("twm-calibration-", ".zc");
        try {
            Files.write(file, contents);
            return PhaseAdjust.load(file, locale);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    static ExperimentFileReader loadMeasurement(byte[] contents, long measuredAt) throws IOException {
        var file = Files.createTempFile("twm-measurement-", ".txt");
        try {
            Files.write(file, contents);
            return new ExperimentFileReader(file, measuredAt);
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
