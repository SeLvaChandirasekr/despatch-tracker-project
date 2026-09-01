package com.abi.service;

import com.abi.dto.ExternalApiTraceVO;

public interface ExternalApiTraceService {

    // Never throws - a tracing failure must never break the calling flow.
    void log(ExternalApiTraceVO trace);

}
