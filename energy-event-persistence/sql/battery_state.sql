create table battery_state(
    id uuid PRIMARY KEY,
    device_id uuid NOT NULL,
    charging_source varchar(50) NOT NULL,
    charging int NOT NULL,
    current_capacity int NOT NULL
    );