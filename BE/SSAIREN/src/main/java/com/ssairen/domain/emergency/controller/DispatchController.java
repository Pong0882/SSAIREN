package com.ssairen.domain.emergency.controller;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.domain.emergency.dto.DispatchListQueryRequest;
import com.ssairen.domain.emergency.dto.DispatchListResponse;
import com.ssairen.domain.emergency.service.DispatchService;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatches")
@RequiredArgsConstructor
@Validated
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

    @Override
    @GetMapping("/fire-state")
    public ResponseEntity<ApiResponse<DispatchListResponse>> getDispatchList(
            @Valid @ModelAttribute DispatchListQueryRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        DispatchListResponse response = dispatchService.getDispatchList(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "출동 목록 조회가 완료되었습니다."));
    }
}
