package com.stormeye.event.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Tests for the the {@link EventAuditService}
 *
 * @author ian@meywood.com
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class EventAuditServiceTest {

    @Autowired
    private EventAuditService eventAuditService;

    @Autowired
    private GridFsOperations gridFsOperations;

    private static final String JSON = "/kafka-events-main.json";

    @BeforeEach
    void setUp() {
        // Delete all files
        gridFsOperations.delete(new Query());
    }

    @Test
    void saveEvent() throws IOException {

        final InputStream in = EventAuditServiceTest.class.getResourceAsStream(JSON);
        final String id = eventAuditService.saveEvent(in);

        assertThat(id, is(notNullValue()));

        // Assert the file was stored
        GridFSFile gridFSFile = gridFsOperations.findOne(new Query(Criteria.where("_id").is(id)));

        // Assert the metadata was correctly populated
        assertThat(gridFSFile, is(notNullValue()));
        assertThat(gridFSFile.getMetadata(), is(notNullValue()));
        assertThat(gridFSFile.getMetadata().getString("source"), is("http://65.21.235.219:9999"));
        assertThat(gridFSFile.getMetadata().getString("type"), is("main"));
        assertThat(gridFSFile.getMetadata().getString("dataType"), is("BlockAdded"));
        assertThat(gridFSFile.getMetadata().getLong("id"), is(65027303L));
    }

    @Test
    void readEvent() throws IOException {

        final InputStream in = EventAuditServiceTest.class.getResourceAsStream(JSON);
        final String id = eventAuditService.saveEvent(in);

        final EventStream readJson = eventAuditService.readEvent(id);
        final byte[] readBytes = IOUtils.toByteArray(readJson);

        assertThat(readJson.getSize(), is((long) readBytes.length));

        // Assert that all bytes are present
        //noinspection resource,ConstantConditions
        assertThat(readBytes, is(EventAuditServiceTest.class.getResourceAsStream(JSON).readAllBytes()));

        // Assert that the EventInfo can be obtained from the bytes stored in GridFS
        final EventInfo eventInfo = new ObjectMapper().readValue(readBytes, EventInfo.class);

        assertThat(eventInfo.getId().get(), is(65027303L));
        assertThat(eventInfo.getSource(), is("http://65.21.235.219:9999"));
        assertThat(eventInfo.getEventType(), is("main"));
        assertThat(eventInfo.getDataType(), is("BlockAdded"));
    }
}