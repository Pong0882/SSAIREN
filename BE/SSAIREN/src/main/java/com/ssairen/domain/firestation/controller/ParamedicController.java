package com.ssairen.domain.firestation.controller;

import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.domain.firestation.dto.ParamedicLoginRequest;
import com.ssairen.domain.firestation.dto.ParamedicLoginResponse;
import com.ssairen.domain.firestation.service.ParamedicService;
import com.ssairen.global.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 구급대원 Controller
 */
@RestController
@RequestMapping("/api/paramedics")
@RequiredArgsConstructor
public class ParamedicController implements ParamedicApi {

    private final ParamedicService paramedicService;

    /**
     * 구급대원 로그인
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ParamedicLoginResponse>> login(
            @Valid @RequestBody ParamedicLoginRequest request
    ) {
        ParamedicLoginResponse response = paramedicService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "로그인에 성공했습니다.")
        );
    }

    /**
     * 전체 구급대원 조회
     */
    @Override
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ParamedicInfo>>> getAllParamedics() {
        List<ParamedicInfo> response = paramedicService.getAllParamedics();
        return ResponseEntity.ok(
                ApiResponse.success(response, "전체 구급대원 목록을 조회했습니다.")
        );
    }
}
