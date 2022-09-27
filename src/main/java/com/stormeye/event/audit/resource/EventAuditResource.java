package com.stormeye.event.audit.resource;

import com.stormeye.event.audit.service.EventAuditService;
import com.stormeye.event.audit.service.EventStream;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
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
 * The REST API for storing an obtaining events as JSON
 *
 * @author ian@meywood.com
 */
@RestController
@RequestMapping("/events")
@OpenAPIDefinition(
        info = @Info(
                title = "Casper Event Audit REST API",
                description = "The events REST APIs that match those of a cspr node event APIs. These APIs also allow for filtering of the streams\n" +
                        " * via the use or a query parameter eg: http://localhost:8080/events/main?query=\"data.DeployProcessed.deploy_hash:2189c51773cf25d566c855ddd165418dace85bc61c40cff6270716c675787084,dataType:BlockAdded\"" +
                        "For more information see  <a href='https://docs.casperlabs.io/dapp-dev-guide/building-dapps/monitoring-events/'><i>Monitoring and Consuming Events<i/></a>",
                contact = @Contact(
                        name = "Stormeye2000",
                        url = "https://github.com/stormeye2000/cspr-producer-audit"
                )
        )
)
public class EventAuditResource {

    private final Logger logger = LoggerFactory.getLogger(EventAuditResource.class);
    private final EventAuditService eventAuditService;

    public EventAuditResource(final EventAuditService eventAuditService) {
        this.eventAuditService = eventAuditService;
    }

    @PostMapping(value = "/audit", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> saveEvent(final HttpServletRequest request) {
        try {
            final String id = eventAuditService.saveEvent(request.getInputStream());
            logger.debug("saved event {}", id);
            return ResponseEntity.ok(id);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/audit/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    private void getEvent(@PathVariable final String id, final HttpServletResponse response) throws IOException {

        logger.debug("getEvent({})", id);

        final EventStream inputStream = eventAuditService.readEvent(id);
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
