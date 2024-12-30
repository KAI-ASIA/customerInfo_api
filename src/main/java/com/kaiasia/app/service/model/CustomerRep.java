package com.kaiasia.app.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRep {
    private String customerID;
    private String responseCode;
    private String customerType;
    private String company;
    private String nationality;
    private String phone;
    private String email;
    private String mainAccount;
    private String name;
    private String trustedType;
    private String lang;
    private String startDate;
    private String endDate;
    private String pwDate;
    private String userLock;
    private String packAge;
    private String userStatus;
}
