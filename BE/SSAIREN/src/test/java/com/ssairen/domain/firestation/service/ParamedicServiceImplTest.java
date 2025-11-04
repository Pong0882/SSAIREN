package com.ssairen.domain.firestation.service;

import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.domain.firestation.dto.ParamedicRegisterRequest;
import com.ssairen.domain.firestation.dto.ParamedicRegisterResponse;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ParamedicServiceImplTest {

    @Autowired
    private ParamedicService paramedicService;

    @Autowired
    private ParamedicRepository paramedicRepository;

    @Autowired
    private FireStateRepository fireStateRepository;

    @Test
    @DisplayName("구급대원 회원가입 - 성공")
    void register_success() {
        // given
        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));

        String uniqueStudentNumber = "TEST" + System.currentTimeMillis();

        ParamedicRegisterRequest request = new ParamedicRegisterRequest(
                uniqueStudentNumber,
                "Password123!",
                "테스트구급대원",
                "소방사",
                fireState.getId()
        );

        // when
        ParamedicRegisterResponse response = paramedicService.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.studentNumber()).isEqualTo(uniqueStudentNumber);
        assertThat(response.name()).isEqualTo("테스트구급대원");
    }

    @Test
    @DisplayName("구급대원 회원가입 - 중복 학번 실패")
    void register_duplicateStudentNumber() {
        // given
        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));

        String studentNumber = "DUP" + System.currentTimeMillis();

        ParamedicRegisterRequest firstRequest = new ParamedicRegisterRequest(
                studentNumber,
                "Password123!",
                "첫번째구급대원",
                "소방사",
                fireState.getId()
        );

        paramedicService.register(firstRequest);

        ParamedicRegisterRequest duplicateRequest = new ParamedicRegisterRequest(
                studentNumber,
                "Password456!",
                "두번째구급대원",
                "소방교",
                fireState.getId()
        );

        // when & then
        assertThatThrownBy(() -> paramedicService.register(duplicateRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARAMEDIC_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("전체 구급대원 조회 - 성공")
    void getAllParamedics_success() {
        // when
        List<ParamedicInfo> result = paramedicService.getAllParamedics();

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("구급대원 회원가입 - 소방서 없음 실패")
    void register_fireStateNotFound() {
        // given
        ParamedicRegisterRequest request = new ParamedicRegisterRequest(
                "TEST99999",
                "Password123!",
                "테스트구급대원",
                "소방사",
                99999  // 존재하지 않는 소방서 ID
        );

        // when & then
        assertThatThrownBy(() -> paramedicService.register(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FIRE_STATION_NOT_FOUND);
    }
}