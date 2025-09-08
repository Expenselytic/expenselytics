package com.expenlytics.web.model;

import org.springframework.hateoas.RepresentationModel;

public class ApiRootModel extends RepresentationModel<ApiRootModel> {
    public final String status;
    public final String apiVersion;

    public ApiRootModel(String apiVersion, String status) {
        this.apiVersion = apiVersion;
        this.status = status;
    }
}
