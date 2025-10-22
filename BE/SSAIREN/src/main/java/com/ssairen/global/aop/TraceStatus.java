package com.ssairen.global.aop;

public record TraceStatus(TraceId traceId, long startTime, String methodSignature) {
}
