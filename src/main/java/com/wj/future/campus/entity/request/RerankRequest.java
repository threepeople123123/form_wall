package com.wj.future.campus.entity.request;

import lombok.Data;

import java.util.List;

@Data
public class RerankRequest {
    private  String query;

    private List<String> documents;
}
