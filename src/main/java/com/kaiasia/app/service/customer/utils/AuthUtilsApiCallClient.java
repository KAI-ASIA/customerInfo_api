package com.kaiasia.app.service.customer.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "dep-api.auth-api")
@Component
@Data
@Slf4j
public class AuthUtilsApiCallClient extends ApiCallClient {
//    private final KaiApiRequestBuilderFactory kaiApiRequestBuilderFactory;
//
//    public Auth1Out callAuth1(String location, ApiRequest request){
//        FundsTransferIn requestData = ObjectAndJsonUtils.fromObject(request.getBody()
//                                                                           .get("transaction"), FundsTransferIn.class);
//        ApiRequest auth1Request = kaiApiRequestBuilderFactory.getBuilder()
//                                                             .api(this.apiName)
//                                                             .apiKey(this.apiKey)
//                                                             .bodyProperties("command", "GET_ENQUIRY")
//                                                             .bodyProperties("enquiry", new Auth1In(this.authenType.get("auth-1"), requestData.getSessionId()))
//                                                             .build();
//        return this.call(location, auth1Request, Auth1Out.class);
//    }
//
//    public Auth3Out callAuth3(String location, ApiRequest request){
//        return this.call(location,request,Auth3Out.class);
//    }
//    @PostConstruct
//    public void init() {
//        System.out.println(this.url + " " + this.apiName+ " " + this.apiKey + " " + this.authenType);
//    }
}
