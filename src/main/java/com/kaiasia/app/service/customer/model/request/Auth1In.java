package com.kaiasia.app.service.customer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * Class này định nghĩa dữ liệu gửi tới Auth-1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth1In {
    private String authenType;
    private String sessionId;
}
