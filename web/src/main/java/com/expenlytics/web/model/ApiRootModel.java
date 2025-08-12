package com.expenlytics.web.model;

import org.springframework.hateoas.RepresentationModel;

public class ApiRootModel extends RepresentationModel<ApiRootModel> {
    public final String apiVersion;

    public ApiRootModel(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
