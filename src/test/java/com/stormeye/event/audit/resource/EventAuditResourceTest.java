package com.stormeye.event.audit.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.stormeye.event.audit.service.EventAuditService;
import com.stormeye.event.audit.service.EventInfo;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author ian@meywood.com
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class EventAuditResourceTest {

    private static final String JSON = "/kafka-events-main.json";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private GridFsOperations gridFsOperations;

    @Autowired
    private EventAuditService eventAuditService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.gridFsOperations.delete(new Query());
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
        this.mockMvc.getDispatcherServlet().setThrowExceptionIfNoHandlerFound(true);
    }

    @Test
    void testSaveEvent() throws Exception {

        byte[] content = EventAuditResourceTest.class.getResourceAsStream(JSON).readAllBytes();


        final String id = mockMvc.perform(post("/events/audit").content(content))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(id, is(notNullValue()));

        final GridFSFile gridFSFile = gridFsOperations.findOne(new Query(Criteria.where("_id").is(id)));

        // Assert the metadata was correctly populated
        assertThat(gridFSFile, is(IsNull.notNullValue()));
        assertThat(gridFSFile.getMetadata(), is(IsNull.notNullValue()));
        assertThat(gridFSFile.getMetadata().getString("source"), is("http://65.21.235.219:9999"));
        assertThat(gridFSFile.getMetadata().getString("type"), is("main"));
        assertThat(gridFSFile.getMetadata().getString("dataType"), is("BlockAdded"));
        assertThat(gridFSFile.getMetadata().getLong("id"), is(65027303L));
    }

    @Test
    void testGetEvent() throws Exception {

        // Save an event
        final String id = eventAuditService.saveEvent(EventAuditResourceTest.class.getResourceAsStream(JSON));

        assertThat(id, is(notNullValue()));

        // Request an event's JSON using its GridFS id
        String json = mockMvc.perform(get("/events/audit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "1163"))
                .andReturn().getResponse().getContentAsString();

        //noinspection resource,ConstantConditions
        assertThat(json, is(new String(EventAuditResourceTest.class.getResourceAsStream(JSON).readAllBytes())));

        // Assert that the EventInfo can be obtained from the bytes stored in GridFS
        final EventInfo eventInfo = new ObjectMapper().readValue(json, EventInfo.class);
;
        assertThat(eventInfo.getId().get(), is(65027303L));
        assertThat(eventInfo.getSource(), is("http://65.21.235.219:9999"));
        assertThat(eventInfo.getEventType(), is("main"));
        assertThat(eventInfo.getDataType(), is("BlockAdded"));
    }
}