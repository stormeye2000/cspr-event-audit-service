package com.stormeye.event.audit.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * Object that provides the metadata for a JSON event.
 *
 * @author ian@meywood.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventInfo {

    private final String eventType;
    private final String source;
    private final String dataType;
    private final Long id;

    @JsonCreator
    public EventInfo(@JsonProperty(value = "type", required = true) final String eventType,
                     @JsonProperty(value = "source", required = true) final String source,
                     @JsonProperty(value = "dataType", required = true) final String dataType,
                     @JsonProperty(value = "id", required = false) final Long id) {
        this.eventType = eventType;
        this.source = source;
        this.dataType = dataType;
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDataType() {
        return dataType;
    }

    public Optional<Long> getId() {
        return Optional.ofNullable(id);
    }

    @Override
    public String toString() {
        return "{" +
                "eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", dataType='" + dataType + '\'' +
                ", id=" + id +
                '}';
    }
}
