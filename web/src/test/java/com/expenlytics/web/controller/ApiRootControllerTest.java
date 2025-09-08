package com.expenlytics.web.controller;

import com.expenlytics.web.model.ApiRootModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApiRootControllerTest {
    @Test
    void getReturnApiRootModelWithLinks() {
        ApiRootController controller = new ApiRootController();

        ApiRootModel model = controller.get().block();

        assertNotNull(model, "ApiRootModel should not be null");
        assertEquals("1.0", model.apiVersion, "API version should be 1.0");
        assertEquals("OK", model.status, "Status should be OK");

        assertEquals(ApiRootController.ROOT, model.getLink("self").orElseThrow().getHref(),
                "Self link should match ROOT");
        assertEquals(AddExpenseController.ADD_EXPENSE, model.getRequiredLink("addExpense").getHref(),
                "AddExpense link should be present");
    }
}
