package thermophysics.backend;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api")
class LaboratoryController {
    record InstallationRequest(String name) {}
    record ConfigurationRequest(Long effectiveFrom, JsonNode settings) {}
    record ExperimentRequest(String name, double lengthMeters, double density, List<Laboratory.Channel> channels) {}
    record RunRequest(UUID calibrationId) {}

    private final Laboratory laboratory;

    LaboratoryController(Laboratory laboratory) {
        this.laboratory = laboratory;
    }

    @PostMapping("/installations")
    @ResponseStatus(HttpStatus.CREATED)
    Laboratory.Installation createInstallation(@RequestBody InstallationRequest request) {
        return laboratory.createInstallation(request.name());
    }

    @GetMapping("/installations")
    List<Laboratory.Installation> installations() {
        return laboratory.installations();
    }

    @PostMapping("/installations/{id}/configurations")
    @ResponseStatus(HttpStatus.CREATED)
    Laboratory.Configuration createConfiguration(@PathVariable UUID id, @RequestBody ConfigurationRequest request) {
        return laboratory.createConfiguration(id, request.effectiveFrom(), request.settings());
    }

    @GetMapping("/installations/{id}/configurations")
    List<Laboratory.Configuration> configurations(@PathVariable UUID id) {
        return laboratory.configurations(id);
    }

    @PostMapping(value = "/configurations/{id}/calibrations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Laboratory.Calibration createCalibration(@PathVariable UUID id, @RequestParam String name,
                                             @RequestParam String numberLocale, @RequestParam List<Integer> channel,
                                             @RequestParam List<MultipartFile> file) throws IOException {
        return laboratory.createCalibration(id, name, numberLocale, channel, file);
    }

    @GetMapping("/configurations/{id}/calibrations")
    List<Laboratory.Calibration> calibrations(@PathVariable UUID id) {
        return laboratory.calibrations(id);
    }

    @PostMapping("/configurations/{id}/experiments")
    @ResponseStatus(HttpStatus.CREATED)
    Laboratory.Experiment createExperiment(@PathVariable UUID id, @RequestBody ExperimentRequest request) {
        return laboratory.createExperiment(id, request.name(), request.lengthMeters(), request.density(), request.channels());
    }

    @GetMapping("/configurations/{id}/experiments")
    List<Laboratory.Experiment> experiments(@PathVariable UUID id) {
        return laboratory.experiments(id);
    }

    @PostMapping(value = "/experiments/{id}/measurements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Laboratory.MeasurementFile addMeasurement(@PathVariable UUID id, @RequestParam long measuredAt,
                                              @RequestParam MultipartFile file) throws IOException {
        return laboratory.addMeasurement(id, measuredAt, file);
    }

    @GetMapping("/experiments/{id}/measurements")
    List<Laboratory.MeasurementFile> measurements(@PathVariable UUID id) {
        return laboratory.measurements(id);
    }

    @PostMapping("/experiments/{id}/runs")
    ResponseEntity<Laboratory.Run> queueRun(@PathVariable UUID id, @RequestBody RunRequest request) {
        Laboratory.Run run = laboratory.queueRun(id, request.calibrationId());
        return ResponseEntity.accepted().location(URI.create("/api/runs/" + run.id())).body(run);
    }

    @GetMapping("/experiments/{id}/runs")
    List<Laboratory.Run> runs(@PathVariable UUID id) {
        return laboratory.runs(id);
    }

    @GetMapping("/runs/{id}")
    Laboratory.RunDetails runDetails(@PathVariable UUID id) {
        return laboratory.runDetails(id);
    }
}
