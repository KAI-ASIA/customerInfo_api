package com.kaiasia.app.service.customer.service;//package com.kaiasia.app.service.customer.service;


import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.customer.exception.ExceptionHandler;
import com.kaiasia.app.service.customer.model.request.CustomerIn;
import com.kaiasia.app.service.customer.model.response.Auth1Out;
import com.kaiasia.app.service.customer.model.response.BaseResponse;
import com.kaiasia.app.service.customer.model.response.CustomerOut;
import com.kaiasia.app.service.customer.model.validation.SuccessGroup;
import com.kaiasia.app.service.customer.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.customer.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.authen.AuthRequest;
import ms.apiclient.authen.AuthTakeSessionResponse;
import ms.apiclient.authen.AuthenClient;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24CustomerInfoResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@KaiService
@Slf4j
@RequiredArgsConstructor
public class CustomerService {
    private final GetErrorUtils apiErrorUtils;
    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private final T24UtilClient t24UtilClient;
    private final ExceptionHandler exceptionHandler;
    private final AuthenClient authenClient;


    @KaiMethod(name = "getCUSTINFO", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, CustomerIn.class, apiErrorUtils, "ENQUIRY");
    }

    @KaiMethod(name = "getCUSTINFO")
    public ApiResponse process(ApiRequest request) {
        CustomerIn requestData = ObjectAndJsonUtils.fromObject(request
                .getBody()
                .get("enquiry"), CustomerIn.class);
        String location = "CustomerInfo-" + requestData.getSessionId() + "-" + System.currentTimeMillis();


        return exceptionHandler.handle(req -> {
            ApiResponse response = new ApiResponse();
            ApiHeader header = new ApiHeader();
            response.setHeader(header);
            ApiBody body = new ApiBody();

            // Call Auth-1 api
            AuthTakeSessionResponse auth1Response = null;
            try {
                auth1Response = authenClient.takeSession(location,
                        AuthRequest.builder()
                                .sessionId(requestData.getSessionId())
                                .build(),
                        request.getHeader());
            } catch (Exception e) {
                throw new RestClientException(location, e);
            }
            if (auth1Response.getError() != null && !ApiError.OK_CODE.equals(auth1Response.getError().getCode())) {
                log.error("{}:{}", location + "#After call Auth-1", auth1Response.getError());
                response.setError(auth1Response.getError());
                return response;
            }


            // Kiểm tra kết quả trả về đủ field không.
            BaseResponse validateAuth1Error = ServiceUtils.validate(ObjectAndJsonUtils.fromObject(auth1Response, Auth1Out.class), SuccessGroup.class);
            if (!validateAuth1Error.getCode().equals(ApiError.OK_CODE)) {
                log.error("{}:{}", location + "#After call Auth-1", validateAuth1Error);
                response.setError(new ApiError(validateAuth1Error.getCode(), validateAuth1Error.getDesc()));
                return response;
            }

            // Tạo cache key
            String cacheKey = "CustomerInfo:" + requestData.getSessionId() + ":" + requestData.getCustomerID();

            // Kiểm tra cache
            ApiResponse cachedResponse = getCachedResponse(cacheKey);
            if (cachedResponse != null) {
                log.info("Cache hit for customer info: {}", cacheKey);
                return cachedResponse;
            }

            // Call T24 API
            T24CustomerInfoResponse t24CustomerInfoResponse = t24UtilClient.getCustomerInfo(location,
                    T24Request.builder()
//                            .customerId(requestData.getCustomerID()
                            .customerId("281692")
                            .build(),
                    request.getHeader());
            log.warn("{}{}", t24CustomerInfoResponse.getId(), t24CustomerInfoResponse.getCifName());

            if (Objects.nonNull(t24CustomerInfoResponse.getError()) && !ApiError.OK_CODE.equals(t24CustomerInfoResponse.getError().getCode())) {
                log.error("Error calling T24 API for customer {} (session {}): {}", requestData.getCustomerID(), requestData.getSessionId(), t24CustomerInfoResponse.getError());
                response.setError(t24CustomerInfoResponse.getError());
                return response;
            }

            HashMap<String, Object> params = new HashMap<>();
            // Kiểm tra kết quả trả về đủ field không.
            BaseResponse validateT24Error = ServiceUtils.validate(ObjectAndJsonUtils.fromObject(t24CustomerInfoResponse, CustomerOut.class), SuccessGroup.class);
            if (!validateT24Error.getCode().equals(ApiError.OK_CODE)) {
                log.error("{}:{}", location + "#After call T2405", validateT24Error);
                params.put("status", ApiConstant.STATUS.ERROR);
                return response;
            }

            params.put("customerID", t24CustomerInfoResponse.getId());
            params.put("customerName", t24CustomerInfoResponse.getCifName());
            params.put("legalId", t24CustomerInfoResponse.getLegalId());
            params.put("company", t24CustomerInfoResponse.getCoCode());
            params.put("language", t24CustomerInfoResponse.getLanguage());
            params.put("phone", t24CustomerInfoResponse.getPhone());
            params.put("email", t24CustomerInfoResponse.getEmail());
            params.put("address", t24CustomerInfoResponse.getAddress());
            params.put("country", t24CustomerInfoResponse.getCountry());
            params.put("legalDocName", t24CustomerInfoResponse.getLegalDocName());
            params.put("legalExpDate", t24CustomerInfoResponse.getLegalExpDate());
            params.put("customerType", t24CustomerInfoResponse.getCustomerType());
            params.put("customerStatus", t24CustomerInfoResponse.getCifStatus());

//            header.setReqType("RESPONSE");
            body.put("enquiry", params);
            response.setBody(body);

            // Lưu vào cache
            cacheResponse(cacheKey, response);

            return response;
        }, request, "CustomerInfo/" + requestData.getSessionId() + "/" + System.currentTimeMillis());
    }

    private ApiResponse getCachedResponse(String cacheKey) {
        try {
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return (ApiResponse) cachedData;
            }
        } catch (Exception e) {
            log.error("Error while accessing Redis: {}", e.getMessage());
        }
        return null;
    }

    private void cacheResponse(String cacheKey, ApiResponse response) {
        try {
            redisTemplate.opsForValue().set(cacheKey, response, 30, TimeUnit.MINUTES); // Lưu cache trong 30 phút
            log.info("Customer info cached with key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Error while caching customer info: {}", e.getMessage());
        }
    }
}