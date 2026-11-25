create table installations (
    id uuid primary key,
    name varchar(200) not null
);

create table configurations (
    id uuid primary key,
    installation_id uuid not null references installations(id),
    version integer not null,
    effective_from bigint not null,
    settings text not null,
    unique (installation_id, version)
);

create table calibrations (
    id uuid primary key,
    configuration_id uuid not null references configurations(id),
    name varchar(200) not null,
    number_locale varchar(80) not null,
    unique (id, configuration_id)
);

create table calibration_files (
    calibration_id uuid not null references calibrations(id),
    channel integer not null,
    file_name varchar(255) not null,
    contents bytea not null,
    primary key (calibration_id, channel)
);

create table experiments (
    id uuid primary key,
    configuration_id uuid not null references configurations(id),
    name varchar(200) not null,
    length_meters double precision not null,
    density double precision not null,
    channels text not null,
    unique (id, configuration_id)
);

create table measurements (
    id uuid primary key,
    experiment_id uuid not null references experiments(id),
    measured_at bigint not null,
    file_name varchar(255) not null,
    contents bytea not null,
    unique (id, experiment_id)
);

create table calculation_runs (
    id uuid primary key,
    experiment_id uuid not null,
    configuration_id uuid not null,
    calibration_id uuid not null,
    requested_at bigint not null,
    status varchar(24) not null,
    unique (id, experiment_id),
    foreign key (experiment_id, configuration_id) references experiments(id, configuration_id),
    foreign key (calibration_id, configuration_id) references calibrations(id, configuration_id)
);

create table run_measurements (
    run_id uuid not null,
    experiment_id uuid not null,
    measurement_id uuid not null,
    result text,
    error_message text,
    primary key (run_id, measurement_id),
    foreign key (run_id, experiment_id) references calculation_runs(id, experiment_id),
    foreign key (measurement_id, experiment_id) references measurements(id, experiment_id)
);

create index calculations_by_status on calculation_runs(status, requested_at);
create index calibrations_by_configuration on calibrations(configuration_id);
create index experiments_by_configuration on experiments(configuration_id);
create index measurements_by_experiment on measurements(experiment_id);
