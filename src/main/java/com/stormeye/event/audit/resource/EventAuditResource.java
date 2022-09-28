package com.stormeye.event.audit.resource;

import com.stormeye.event.audit.service.EventAuditService;
import com.stormeye.event.audit.service.EventStream;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The REST API for storing an obtaining events as JSON.
 *
 * @author ian@meywood.com
 */
@RestController
@RequestMapping("/events")
@OpenAPIDefinition(
        info = @Info(
                title = "Casper Event Audit REST API",
                description = "Stores a Obtains JSON representation of the Casper Java SDK com.casper.sdk.model.event.Event object." +
                        "For more information see  <a href='https://docs.casperlabs.io/dapp-dev-guide/building-dapps/monitoring-events/'><i>Monitoring and Consuming Events<i/></a>",
                contact = @Contact(
                        name = "Stormeye2000",
                        url = "https://github.com/stormeye2000/cspr-event-audit-service"
                )
        )
)
public class EventAuditResource {

    private final Logger logger = LoggerFactory.getLogger(EventAuditResource.class);
    private final EventAuditService eventAuditService;

    public EventAuditResource(final EventAuditService eventAuditService) {
        this.eventAuditService = eventAuditService;
    }

    /**
     * Stores a JSON representation of ra aw {@link com.casper.sdk.model.event.Event}.
     *
     * @param request the request whose input stream the JSON will be read from
     * @return the internal ID of the stored JSON event. Note this is not the event ID, but the ID that is created for
     * the JSON when persisted
     */
    @PostMapping(value = "/audit", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Stores a JSON representation of a com.casper.sdk.model.event.Event and returns it's internal storage ID")
    public ResponseEntity<String> saveEvent(final HttpServletRequest request) {
        try {
            final String id = eventAuditService.saveEvent(request.getInputStream());
            logger.debug("saved event {}", id);
            return ResponseEntity.ok(id);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtains a JSON representation of a raw {@link com.casper.sdk.model.event.Event}. Using the internal ID that was generated
     * when it was stored.
     *
     * @param id the internal ID of the JSON to obtain, not the ID of the event
     */
    @GetMapping(value = "/audit/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtains a JSON representation of a com.casper.sdk.model.event.Event using it's internal storage ID")
    private void getEvent(@PathVariable final String id, final HttpServletResponse response) throws IOException {

        logger.debug("getEvent({})", id);

        final EventStream inputStream = eventAuditService.getEventById(id);
        final ServletOutputStream outputStream = response.getOutputStream();

        response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(inputStream.getSize()));
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());

        IOUtils.copy(inputStream, outputStream);
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }
}
