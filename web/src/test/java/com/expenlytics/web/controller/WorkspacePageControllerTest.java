package com.expenlytics.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class WorkspacePageControllerTest {
    @Test
    void servesAngularEntryForDirectWorkspaceVisits() {
        var client = WebTestClient.bindToController(new WorkspacePageController()).build();
        for (String path : new String[] {"/workspace", "/workspace/"}) {
            client.get().uri(path).exchange().expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("text/html")
                .expectBody(String.class).value(body -> org.junit.jupiter.api.Assertions.assertTrue(body.contains("<app-root>")));
        }
        client.get().uri("/api/v1/unknown").exchange().expectStatus().isNotFound();
    }
}
