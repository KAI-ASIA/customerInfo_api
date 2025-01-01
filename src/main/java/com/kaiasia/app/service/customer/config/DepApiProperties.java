package com.kaiasia.app.service.customer.config;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepApiProperties {
    private String url;
    private long timeout;
    private String apiKey;
    private String apiName;
}
