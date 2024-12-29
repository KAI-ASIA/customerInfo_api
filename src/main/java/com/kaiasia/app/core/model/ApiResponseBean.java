package com.kaiasia.app.core.model;

import lombok.Data;

import java.util.Date;

@Data
// Model cho response được lưu trong database
public class ApiResponseBean {
    private String reqId;
    private Date receiveTime;
    private Date responseTime;
    private String requestMsg;
    private String responseMsg;
    private String requestApi;
    private String requestNode;
    private String processNode;
    private String status;
    private Date endProcessTime;
    private Date startProcessTime;
    private String responseNode;
    private String receiveNode;
}
