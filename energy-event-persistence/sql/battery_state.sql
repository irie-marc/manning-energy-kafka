create table if not exists battery_state(
    id uuid PRIMARY KEY,
    device_id uuid NOT NULL,
    charging_source varchar(50) NOT NULL,
    charging int NOT NULL,
    current_capacity int NOT NULL
    );

create table if not exists device_message_metadata(
    id uuid PRIMARY KEY,
    device_id uuid NOT NULL,
    message_id uuid NOT NULL UNIQUE,
    is_parsed boolean NOT NULL,
    time_stamp_arrival TIMESTAMP NOT NULL,
    time_stamp_parse_start TIMESTAMP NOT NULL,
    number_of_events int NOT NULL
    );

 create table if not exists device_message_metadata_parse_info(
     id uuid PRIMARY KEY,
     message_id uuid NOT NULL,
     time_stamp_parse_completed TIMESTAMP NOT NULL,
     parsing_duration_nano_seconds BIGINT NOT NULL,
     FOREIGN KEY (message_id) REFERENCES device_message_metadata(message_id)
     );
