package com.kaiasia.app.core.async;

import com.kaiasia.app.core.model.ApiError;
import com.kaiasia.app.core.model.ApiRequest;

// Interface định nghĩa phương thức validate request
public interface IValidate {
    ApiError validate(ApiRequest request);
}
