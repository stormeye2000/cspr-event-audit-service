package com.stormeye.event.audit.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.PostConstruct;

/**
 * Spring Web Configuration class.
 *
 * @author ian@meywood.com
 */
@Component
public class WebConfig {

    private final DispatcherServlet dispatcherServlet;

    public WebConfig(final DispatcherServlet dispatcherServlet) {
        this.dispatcherServlet = dispatcherServlet;
    }

    @PostConstruct
    public void configureDispatcherServlet() {
        this.dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
    }
}
