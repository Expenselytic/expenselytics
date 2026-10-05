package com.expenlytics.web.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serve the Angular entry point for direct workspace visits and refreshes. */
@RestController
public class WorkspacePageController {
    @GetMapping(value = {"/workspace", "/workspace/"}, produces = MediaType.TEXT_HTML_VALUE)
    public Resource workspace() {
        return new ClassPathResource("static/index.html");
    }
}
