package com.kmong.application;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class MessageFacadeCriteria {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class ProcessEsinInfra{
        private List<Map<String,Object>> payload;
        private Map<String, Object> row;
        private String productOrderId;
        private boolean isApiRequest;

    }
}
