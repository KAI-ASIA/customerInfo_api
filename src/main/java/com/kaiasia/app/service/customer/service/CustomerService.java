package com.kaiasia.app.service.customer.service;

import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.customer.config.DepApiConfig;
import com.kaiasia.app.service.customer.config.DepApiProperties;
import com.kaiasia.app.service.customer.config.KaiApiRequestBuilderFactory;
import com.kaiasia.app.service.customer.utils.ApiCallHelper;
import com.kaiasia.app.service.customer.utils.ObjectAndJsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import java.util.HashMap;

@KaiService
@Slf4j
public class CustomerService {
    @Autowired
    GetErrorUtils apiErrorUtils;
    @Autowired
    private DepApiConfig depApiConfig;
    @Autowired
    private KaiApiRequestBuilderFactory kaiApiRequestBuilderFactory;
    @Autowired
    private GetErrorUtils getErrorUtils;


    @KaiMethod(name = "getUSER_PROFILE", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
        HashMap enquiry = (HashMap) req.getBody().get("enquiry");
        if (enquiry == null) {
            error = apiErrorUtils.getError("804", new String[]{"Enquiry part is required"});
            return error;
        }
        String[] enquiryFields = new String[]{"authenType", "sessionId", "userID"};
        for (String enquiryField : enquiryFields) {
            if (!enquiry.containsKey(enquiryField) || StringUtils.isEmpty(enquiry.get(enquiryField))) {
                error = apiErrorUtils.getError("804", new String[]{enquiryField + " is required"});
                return error;
            }
        }
        return error;
    }

    @KaiMethod(name = "getUSER_PROFILE")
    public ApiResponse process(ApiRequest request) {
        ApiResponse response = new ApiResponse();
        ApiError error;
        ApiHeader header = new ApiHeader();
        header.setReqType("RESPONSE");
        response.setHeader(header);

        HashMap requestCustomer = (HashMap) request.getBody().get("enquiry");
        String location = "CustomerInfo/" + requestCustomer.get("sessionId") + "/" + System.currentTimeMillis();
        // Call aut check sessionId
        DepApiProperties authApiProperties = depApiConfig.getAuthApi();
        // Request kiểm tra session
        HashMap<String, Object> sessionCheckEnquiry = new HashMap<>();
        sessionCheckEnquiry.put("authenType", "checkSession");
        sessionCheckEnquiry.put("sessionId", requestCustomer.get("sessionId"));
        sessionCheckEnquiry.put("userID", requestCustomer.get("userID"));
        ApiRequest authReq = kaiApiRequestBuilderFactory.getBuilder()
                .api(authApiProperties.getApiName())
                .apiKey(authApiProperties.getApiKey())
                .bodyProperties("command", "GET_ENQUIRY")
                .bodyProperties("enquiry", sessionCheckEnquiry)
                .build();

        String userName = "";
        try {
            ApiResponse authRes = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(authReq), ApiResponse.class);
            error = authRes.getError();
            if (error != null || !"OK".equals(authRes.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call auth", error);
                response.setError(error);
                return response;
            }
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling Auth-1", e.getMessage());
            error = getErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;

        }
        // Call T24 API to retrieve eBank information
        DepApiProperties t24ApiProperties = depApiConfig.getAuthApi();
        HashMap<String, Object> t24Request = new HashMap<>();
        t24Request.put("userID", requestCustomer.get("userID"));
        t24Request.put("sessionId", requestCustomer.get("sessionId"));

        ApiRequest t24ApiReq = kaiApiRequestBuilderFactory.getBuilder()
                .api(t24ApiProperties.getApiName())
                .apiKey(t24ApiProperties.getApiKey())
                .bodyProperties("command", "GET_USER_PROFILE")
                .bodyProperties("enquiry", t24Request)
                .build();

        try {
            ApiResponse t24ApiRes = ApiCallHelper.call(t24ApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(t24ApiReq), ApiResponse.class);
            error = t24ApiRes.getError();
            if (error != null || !"OK".equals(t24ApiRes.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call T24", error);
                response.setError(error);
                return response;
            }

            // Build the eBank response body
            HashMap userProfile = (HashMap) t24ApiRes.getBody().get("enquiry");
            ApiBody body = new ApiBody();
            body.put("enquiry", userProfile);
            response.setBody(body);
        } catch (
                Exception e) {
            log.error("{}:{}", location + "#Calling T24", e.getMessage());
            error = apiErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        return response;
    }
}

