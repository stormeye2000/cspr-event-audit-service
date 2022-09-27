package com.stormeye.event.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * @author ian@meywood.com
 */
class EventInfoTest {

    private static final String JSON = "/kafka-events-main.json";
    private EventInfo eventInfo;

    @BeforeEach
    void setUp() throws IOException {
        eventInfo = new ObjectMapper().readValue(EventInfoTest.class.getResourceAsStream(JSON), EventInfo.class);
    }

    @Test
    void getSource() {
        assertThat(eventInfo.getSource(), is("http://65.21.235.219:9999"));
    }

    @Test
    void getEventType() {
        assertThat(eventInfo.getEventType(), is("main"));
    }

    @Test
    void getDataType() {
        assertThat(eventInfo.getDataType(), is("BlockAdded"));
    }

    @Test
    void getId() {
        assertThat(eventInfo.getId().isPresent(), is(true));
        assertThat(eventInfo.getId().get(), is(65027303L));
    }
}