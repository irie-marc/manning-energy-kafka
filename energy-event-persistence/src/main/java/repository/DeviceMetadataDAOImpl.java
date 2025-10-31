package repository;

import org.jdbi.v3.sqlobject.customizer.Bind;

import java.util.UUID;

public class DeviceMetadataDAOImpl {

    public long getParsing_duration_nano_seconds() {
        return parsing_duration_nano_seconds;
    }

    public void setParsing_duration_nano_seconds(long parsing_duration_nano_seconds) {
        this.parsing_duration_nano_seconds = parsing_duration_nano_seconds;
    }


    public long getTime_stamp_parse_completed() {
        return time_stamp_parse_completed;
    }

    public void setTime_stamp_parse_completed(long time_stamp_parse_completed) {
        this.time_stamp_parse_completed = time_stamp_parse_completed;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDevice_id() {
        return device_id;
    }

    public void setDevice_id(UUID device_id) {
        this.device_id = device_id;
    }

    public UUID getMessage_id() {
        return message_id;
    }

    public void setMessage_id(UUID message_id) {
        this.message_id = message_id;
    }

    public boolean isIs_parsed() {
        return is_parsed;
    }

    public void setIs_parsed(boolean is_parsed) {
        this.is_parsed = is_parsed;
    }

    public long getTime_stamp_arrival() {
        return time_stamp_arrival;
    }

    public void setTime_stamp_arrival(long time_stamp_arrival) {
        this.time_stamp_arrival = time_stamp_arrival;
    }

    public long getTime_stamp_parse_start() {
        return time_stamp_parse_start;
    }

    public void setTime_stamp_parse_start(long time_stamp_parse_start) {
        this.time_stamp_parse_start = time_stamp_parse_start;
    }

    public long getNumber_of_events() {
        return number_of_events;
    }

    public void setNumber_of_events(long number_of_events) {
        this.number_of_events = number_of_events;
    }

    private UUID id;
    private UUID device_id;
    private UUID message_id;
    private boolean is_parsed;
    private long time_stamp_arrival;
    private long time_stamp_parse_start;
    private long number_of_events;
    private long parsing_duration_nano_seconds;
    private long time_stamp_parse_completed;
}
