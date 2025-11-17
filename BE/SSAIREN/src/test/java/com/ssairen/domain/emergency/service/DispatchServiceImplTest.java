package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DispatchServiceImplTest {

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private FireStateRepository fireStateRepository;

    @Autowired
    private ParamedicRepository paramedicRepository;

    @Test
    @DisplayName("출동 배정 생성 - 성공")
    void createDispatch_success() {
        // given
        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));

        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        DispatchCreateRequest request = new DispatchCreateRequest(
                fireState.getId(),
                paramedic.getId(),
                "TEST-2025-001",
                "화재",
                "건물화재",
                "홍길동",
                "010-1234-5678",
                "서울시 강남구 테헤란로 123",
                "화재 발생",
                "일반",
                1,
                "강남119안전센터",
                LocalDateTime.now()
        );

        // when
        DispatchCreateResponse response = dispatchService.createDispatch(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.disasterType()).isEqualTo("화재");
        assertThat(response.locationAddress()).isEqualTo("서울시 강남구 테헤란로 123");
    }

    @Test
    @DisplayName("출동 배정 생성 - 소방서 없음 실패")
    void createDispatch_fireStateNotFound() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        DispatchCreateRequest request = new DispatchCreateRequest(
                99999,  // 존재하지 않는 ID
                paramedic.getId(),
                "TEST-2025-002",
                "화재",
                "건물화재",
                "홍길동",
                "010-1234-5678",
                "서울시 강남구 테헤란로 456",
                "화재 발생",
                "일반",
                1,
                "강남119안전센터",
                LocalDateTime.now()
        );

        // when & then
        assertThatThrownBy(() -> dispatchService.createDispatch(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FIRE_STATE_NOT_FOUND);
    }

    @Test
    @DisplayName("출동 배정 생성 - 구급대원 없음 실패")
    void createDispatch_paramedicNotFound() {
        // given
        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));

        DispatchCreateRequest request = new DispatchCreateRequest(
                fireState.getId(),
                99999,  // 존재하지 않는 ID
                "TEST-2025-003",
                "화재",
                "건물화재",
                "홍길동",
                "010-1234-5678",
                "서울시 강남구 테헤란로 789",
                "화재 발생",
                "일반",
                1,
                "강남119안전센터",
                LocalDateTime.now()
        );

        // when & then
        assertThatThrownBy(() -> dispatchService.createDispatch(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARAMEDIC_NOT_FOUND);
    }

    @Test
    @DisplayName("출동 목록 조회 - 성공")
    void getDispatchList_success() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when
        var response = dispatchService.getDispatchList(
                paramedic.getId(),
                new com.ssairen.domain.emergency.dto.DispatchListQueryRequest(null, 10)
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.fireState()).isNotNull();
        assertThat(response.dispatches()).isNotNull();
        assertThat(response.pagination()).isNotNull();
    }

    @Test
    @DisplayName("출동 목록 조회 - 구급대원 없음 실패")
    void getDispatchList_paramedicNotFound() {
        // when & then
        assertThatThrownBy(() -> dispatchService.getDispatchList(
                99999,  // 존재하지 않는 ID
                new com.ssairen.domain.emergency.dto.DispatchListQueryRequest(null, 10)
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARAMEDIC_NOT_FOUND);
    }
}