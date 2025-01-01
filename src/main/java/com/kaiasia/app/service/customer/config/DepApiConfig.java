package com.kaiasia.app.service.customer.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

@Component
@ConfigurationProperties(prefix = "dex-api")
@Data
public class DepApiConfig {
    @Autowired
    private Environment env;
    private DepApiProperties authApi;
    private DepApiProperties t24utilsApi;
    public DepApiProperties getApiProperties(String name){
        String prefix = "dep-api." + name;
        return DepApiProperties.builder()
                .url(env.getProperty(prefix) + ".url")
                .apiKey(env.getProperty(prefix) + ".url")
                .apiName(env.getProperty(prefix) + ".url")
                .timeout(Long.parseLong(StringUtils.defaultString(env.getProperty(prefix + ".timeout"))))
                .build();
    }
}
