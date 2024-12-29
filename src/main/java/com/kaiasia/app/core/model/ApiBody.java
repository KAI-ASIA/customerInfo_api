package com.kaiasia.app.core.model;

import java.util.TreeMap;

//  Lớp TreeMap để chứa body của API response
public class ApiBody extends TreeMap<String, Object> {
    public ApiBody() {
        this.put("status", "OK");
    }
}
