package com.kaiasia.app.service.customer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Class này dùng để định nghĩa dữ liệu trả ra từ Customer và cũng có thể trả ra từ T2405
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CustomerOut extends BaseResponse {
    private String customerID;
    private String customerName;
    private String legalId;
    private String company;
    private String language;
    private String phone;
    private String email;
    private String address;
    private String country;
    private String legalDocName;
    private String legalExpDate;
    private String customerType;
    private String customerStatus;
}
