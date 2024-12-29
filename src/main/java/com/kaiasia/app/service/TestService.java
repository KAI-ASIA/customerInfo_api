package com.kaiasia.app.service;



import com.kaiasia.app.core.model.ApiBody;
import com.kaiasia.app.core.model.ApiError;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.core.model.ApiResponse;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.HashMap;
import java.util.Map;

@KaiService
@Slf4j
public class TestService {

    @Autowired
    GetErrorUtils apiErrorUtils;


    @KaiMethod(name = "TestService", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req){
       return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
    }

    @KaiMethod(name = "TestService")
    public ApiResponse process(ApiRequest req){
        ApiResponse apiResponse = new ApiResponse();
        ApiBody apiBody = new ApiBody();
        try {
            Map<String, Object> bodyEnq = new HashMap<>();

            apiResponse.setBody(apiBody);
            return apiResponse;
        } catch (Exception e){
            log.error("Exception", e);
            ApiError apiError = apiErrorUtils.getError("999", new String[]{e.getMessage()});
            apiResponse.setError(apiError);
            return apiResponse;
        }

    }



}
