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

    /** The type/topic of the event main, delpoys, sigs */
    private final String eventType;
    /** The URL of the casper node that emitted the event */
    private final String source;
    /** The type of the data: in the event e.g. DeployAdded etc */
    private final String dataType;
    /** The optional ID of the event */
    private final Long id;
    /** The version of the casper node when the event was emitted */
    private final String version;

    @JsonCreator
    public EventInfo(@JsonProperty(value = "type", required = true) final String eventType,
                     @JsonProperty(value = "source", required = true) final String source,
                     @JsonProperty(value = "dataType", required = true) final String dataType,
                     @JsonProperty(value = "id") final Long id,
                     @JsonProperty(value = "version") final String version) {
        this.eventType = eventType;
        this.source = source;
        this.dataType = dataType;
        this.id = id;
        this.version = version;
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

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "{" +
                "eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", dataType='" + dataType + '\'' +
                ", id=" + id +
                ", version=" +  version +
                '}';
    }
}
