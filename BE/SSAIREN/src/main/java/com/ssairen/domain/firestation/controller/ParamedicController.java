package com.ssairen.domain.firestation.controller;

import com.ssairen.domain.firestation.dto.ParamedicLoginRequest;
import com.ssairen.domain.firestation.dto.ParamedicLoginResponse;
import com.ssairen.domain.firestation.service.ParamedicService;
import com.ssairen.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 구급대원 Controller
 */
@RestController
@RequestMapping("/api/paramedics")
@RequiredArgsConstructor
public class ParamedicController {

    private final ParamedicService paramedicService;

    /**
     * 구급대원 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ParamedicLoginResponse>> login(
            @Valid @RequestBody ParamedicLoginRequest request
    ) {
        ParamedicLoginResponse response = paramedicService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "로그인에 성공했습니다.")
        );
    }
}