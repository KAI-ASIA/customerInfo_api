package com.kaiasia.app.core.async;

import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.core.model.ApiResponse;

// Interface định nghĩa phương thức xử lý request
public interface IProcess {
    ApiResponse process(ApiRequest req);
}
