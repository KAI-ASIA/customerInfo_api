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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import java.util.HashMap;

@KaiService
@Slf4j
public class CustomerService {

    @Autowired
    private GetErrorUtils apiErrorUtils;
    @Autowired
    private DepApiConfig depApiConfig;
    @Autowired
    private KaiApiRequestBuilderFactory kaiApiRequestBuilderFactory;
    @Autowired
    private GetErrorUtils getErrorUtils;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
        String sessionId = (String) requestCustomer.get("sessionId");
        String userId = (String) requestCustomer.get("userID");
        String cacheKey = "userProfile:" + sessionId + ":" + userId;

        // Kiểm tra Auth bằng Auth-1
        String location = "AuthAPI/validateSession/" + sessionId + "/" + System.currentTimeMillis();
        DepApiProperties authApiProperties = depApiConfig.getAuthApi();
        HashMap<String, Object> authRequestEnquiry = new HashMap<>();
        authRequestEnquiry.put("authenType", "takeSession");
        authRequestEnquiry.put("sessionId", sessionId);

        ApiRequest authRequest = kaiApiRequestBuilderFactory.getBuilder()
                .api(authApiProperties.getApiName())
                .apiKey(authApiProperties.getApiKey())
                .bodyProperties("command", "GET_ENQUIRY")
                .bodyProperties("enquiry", authRequestEnquiry)
                .build();

        String username = "";
        try {
            ApiResponse authResponse = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(authRequest), ApiResponse.class);
            error = authResponse.getError();
            if (error != null || !"OK".equals(authResponse.getBody().get("status"))) {
                log.error("{}: Auth failed with error - {}", location, error.getCode());
                response.setError(error);
                return response;
            }
            username = (String) ((HashMap) authResponse.getBody().get("enquiry")).get("username");
            log.info("Auth-1 validated successfully for username: {}", username);
        } catch (Exception e) {
            log.error("{}: Exception while calling Auth-1 - {}", location, e.getMessage());
            response.setError(apiErrorUtils.getError("999", new String[]{e.getMessage()}));
            return response;
        }

        // Kiểm tra cache trước
        ApiResponse cachedResponse = getCachedUserProfile(cacheKey);
        if (cachedResponse != null) {
            log.info("Cache hit for user profile: {}", cacheKey);
            return cachedResponse; // Trả về dữ liệu từ cache nếu có
        }

        // Nếu không có trong cache, gọi API T24
        location = "CustomerInfo/" + sessionId + "/" + System.currentTimeMillis();
        DepApiProperties t24ApiProperties = depApiConfig.getAuthApi();
        HashMap<String, Object> t24Request = new HashMap<>();
        t24Request.put("userID", userId);
        t24Request.put("sessionId", sessionId);

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
                log.error("{}: T24 API failed with error - {}", location, error.getCode());
                response.setError(error);
                return response;
            }

            // Lấy thông tin người dùng từ T24 API
            HashMap userProfile = (HashMap) t24ApiRes.getBody().get("enquiry");
            ApiBody body = new ApiBody();
            body.put("enquiry", userProfile);
            response.setBody(body);

            // Lưu kết quả vào Redis
            cacheUserProfile(cacheKey, response);

        } catch (Exception e) {
            log.error("{}: Exception while calling T24 API - {}", location, e.getMessage());
            error = apiErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        return response;
    }
    private ApiResponse getCachedUserProfile(String cacheKey) {
        try {
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return (ApiResponse) cachedData; // Trả về dữ liệu từ Redis nếu có
            }
        } catch (Exception e) {
            log.error("Error while accessing Redis: {}", e.getMessage());
        }
        return null;
    }

    private void cacheUserProfile(String cacheKey, ApiResponse response) {
        try {
            redisTemplate.opsForValue().set(cacheKey, response, 30 * 60); // Lưu cache trong 30 phút
            log.info("User profile cached with key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Error while caching user profile: {}", e.getMessage());
        }
    }
}
