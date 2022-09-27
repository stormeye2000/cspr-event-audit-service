package com.stormeye.event.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.stormeye.event.audit.exception.NotFoundException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


/**
 * The service for storing and retrieving JSON Events to and from GridFS.
 *
 * @author ian@meywood.com
 */
@Service
public class EventAuditService {

    private static final String NOT_FOUND_MESSAGE = "Unable to find event with id ";
    public static final String JSON = ".json";

    /** The JSON parser used to obtain the metadata from the events */
    private final ObjectMapper mapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(EventAuditService.class);

    private final GridFsOperations gridFsOperations;

    public EventAuditService(GridFsOperations gridFsOperations) {
        this.gridFsOperations = gridFsOperations;
    }

    /**
     * Saves an event as JSON in GridFS
     *
     * @param eventStream the stream to read the event from as JSON
     * @return the ID of the stored grids file
     * @throws IOException if an I/O error occurs on reading the evenStream
     */
    public String saveEvent(final InputStream eventStream) throws IOException {

        final byte[] bytes = IOUtils.toByteArray(eventStream);

        final EventInfo eventInfo = mapper.readValue(bytes, EventInfo.class);

        logger.debug("Saving event: {}", eventInfo);

        // Obtain the event ID
        final DBObject metadata = BasicDBObjectBuilder.start()
                .append("type", eventInfo.getEventType())
                .append("dataType", eventInfo.getDataType())
                .append("source", eventInfo.getSource())
                .append("bytes", bytes.length)
                .get();

        // Add the event ID if it exists
        eventInfo.getId().ifPresent(id -> metadata.put("id", id));

        return gridFsOperations.store(
                new ByteArrayInputStream(bytes),
                buildFilename(eventInfo),
                "application/json",
                metadata
        ).toHexString();
    }


    /**
     * Obtains an input stream to a JSON Event from GridFS.
     *
     * @param id of the GridFs file to obtain
     * @return the input stream to read the file from
     */
    public EventStream readEvent(final String id) {

        logger.debug("reading event {}", id);

        final GridFSFile gridFsFile = gridFsOperations.findOne(new Query(Criteria.where("_id").is(id)));

        if (gridFsFile == null) {
            throw new NotFoundException(NOT_FOUND_MESSAGE + id);
        }

        GridFsResource resource = gridFsOperations.getResource(gridFsFile);

        if (!resource.exists()) {
            throw new NotFoundException(NOT_FOUND_MESSAGE + id);
        }
        return new EventStream(resource.getContent(), gridFsFile.getLength());
    }

    private String buildFilename(final EventInfo eventInfo) {
        if (eventInfo.getId().isPresent()) {
            return eventInfo.getId().get() + JSON;
        } else {
            return "_" + UUID.randomUUID() + JSON;
        }
    }

}
