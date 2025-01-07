package com.kaiasia.app.service.customer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * Class này định nghĩa dữ liệu cần gửi tới Customer và T24
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerIn {
    @NotBlank(message = "Authentication type is required")
    private String authenType;
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    @NotBlank(message = "Customer ID is required")
    private String customerID;
}
