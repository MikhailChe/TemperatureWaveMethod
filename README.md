# TemperatureWaveMethod

Java Swing application for calculating thermal diffusivity using the temperature wave method.

## Build and run

Use Java 17 or newer to start the Gradle Wrapper. Gradle uses JDK 27 for
compilation, tests, and application runs. If it cannot find a local JDK 27, it
downloads one through the Foojay toolchain resolver and caches it in Gradle
User Home. The first download requires internet access.

```sh
./gradlew build
./gradlew test
./gradlew run
```

On Windows, use `gradlew.bat` in place of `./gradlew`.

The test report is at `build/reports/tests/test/index.html`. Distribution archives are under `build/distributions`.

## Calculation library

Use the `core` Gradle module from a server job. Load the phase calibration once
with the number locale of the calibration file and reuse it for measurements
made with that installation configuration.

```java
PhaseAdjust calibration = PhaseAdjust.load(calibrationFile, Locale.FRANCE);
BaseSignalID channel = new BaseSignalID();
channel.phaseAdjust = calibration;
var calculator = new MeasurementCalculator(
        new MeasurementCalculator.SampleProperties(lengthMeters, densityKgPerCubicMeter),
        Arrays.asList(null, channel));
Measurement result = calculator.calculate(measurementFile, measuredAt.toEpochMilli());
```

Channel positions match file columns, starting with the reference at index zero.
Use `null` for unused columns. Supply the time of measurement explicitly and
treat channel settings as read-only while the calculator uses them.

Run `./gradlew :core:test` to test the library independently.

## Backend

Run `./gradlew :backend:bootRun`. The API listens at
`http://127.0.0.1:8080/api` and uses a file-backed H2 database by default.
For PostgreSQL, set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
and `SPRING_DATASOURCE_PASSWORD`. Flyway creates the schema.
The API currently assumes a trusted local caller.

### Process a series

1. Create the installation with `POST /api/installations` and
   `{"name":"Laboratory installation"}`.
2. Record a configuration with `POST /api/installations/{installationId}/configurations`
   and `{"effectiveFrom":1760000000000,"settings":{"laser":"A","gain":270}}`.
   The configuration is immutable. Record a new version when the equipment changes.
3. Upload a phase calibration with `POST /api/configurations/{configurationId}/calibrations`.
   Send multipart fields `name`, `numberLocale`, `channel`, and `file`.
   Repeat `channel` and `file` in matching order for multiple columns.
   For example, use `numberLocale=ru-RU` for decimal commas in the calibration.
4. Create each experiment with `POST /api/configurations/{configurationId}/experiments`
   and `{"name":"Sample A","lengthMeters":0.002,"density":8000,"channels":[{"column":1,"inverse":false}]}`.
   Use meters and kg/m3 for the sample. Column zero is the reference.
5. Upload each measurement with `POST /api/experiments/{experimentId}/measurements`.
   Send multipart fields `measuredAt` and `file`. Both `measuredAt` and
   `effectiveFrom` use Unix milliseconds. Use the configuration ID from acquisition
   when importing an older experiment.
6. Submit a calculation with `POST /api/experiments/{experimentId}/runs` and
   `{"calibrationId":"<id from step 3>"}`. Reuse that ID for other experiments on
   the same configuration. A calibration from another configuration or one
   missing a requested channel returns HTTP 409.
7. Follow the `Location` from HTTP 202 to `GET /api/runs/{runId}`.
   Each run captures the files present when it was submitted. Poll until its
   status changes from `QUEUED` to `COMPLETED`, `PARTIAL`, or `FAILED`.
   The response contains the numerical result or error for each file.

The installation, configuration, calibration, experiment, measurement, and run
collection URLs also support GET. The backend stores original uploads and
results in the database. The upload limit is 16 MB per file.

Run `./gradlew :backend:test` for the HTTP integration test.

## React frontend

Start the backend with `./gradlew :backend:bootRun`. Run these commands in another
terminal with Node.js 24.

```sh
cd frontend
npm ci
npm run dev
```

Open `http://127.0.0.1:5173`. Vite proxies `/api` to
`http://127.0.0.1:8080`. Set `TWM_API_URL` before starting Vite to use a different
backend URL.

Choose the installation and configuration version, then select a calibration
in the series selector. The selection is stored in the browser for that version
and is used when starting a run. Switching to a different version gives that
version its own selection. The run history shows the calibration actually used.

`npm run build` type-checks the frontend and produces `frontend/dist`.
`npm run preview` serves that build with the same API proxy. For deployment,
serve the static build and route same-origin `/api` requests to Spring Boot.
