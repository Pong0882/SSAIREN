package com.ssairen.domain.emergency.controller;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.domain.emergency.service.DispatchService;
import com.ssairen.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispatches")
@RequiredArgsConstructor
public class DispatchController implements DispatchApi {

    private final DispatchService dispatchService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<DispatchCreateResponse>> createDispatch(
            @Valid @RequestBody DispatchCreateRequest request
    ) {
        DispatchCreateResponse response = dispatchService.createDispatch(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(response, "출동지령이 생성되었습니다."));
    }
}
