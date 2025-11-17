package com.ssairen.domain.emergency.validation;

import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyReportValidatorTest {

    @Mock
    private FireStateRepository fireStateRepository;

    @InjectMocks
    private EmergencyReportValidator validator;

    private Integer fireStateId;
    private FireState fireState;

    @BeforeEach
    void setUp() {
        fireStateId = 1;
        fireState = FireState.builder()
                .id(fireStateId)
                .build();
    }

    @Test
    @DisplayName("소방서 존재 검증 - 성공")
    void validateFireStateExists_success() {
        // given
        when(fireStateRepository.findById(fireStateId))
                .thenReturn(Optional.of(fireState));

        // when
        FireState result = validator.validateFireStateExists(fireStateId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(fireStateId);
        verify(fireStateRepository).findById(fireStateId);
    }

    @Test
    @DisplayName("소방서 존재 검증 - 소방서가 없는 경우")
    void validateFireStateExists_notFound() {
        // given
        when(fireStateRepository.findById(fireStateId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> validator.validateFireStateExists(fireStateId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FIRE_STATE_NOT_FOUND);

        verify(fireStateRepository).findById(fireStateId);
    }

    @Test
    @DisplayName("소방서 존재 검증 - 여러 ID로 조회")
    void validateFireStateExists_multipleIds() {
        // given
        Integer id1 = 1;
        Integer id2 = 2;
        Integer id3 = 3;

        FireState state1 = FireState.builder().id(id1).build();
        FireState state2 = FireState.builder().id(id2).build();

        when(fireStateRepository.findById(id1)).thenReturn(Optional.of(state1));
        when(fireStateRepository.findById(id2)).thenReturn(Optional.of(state2));
        when(fireStateRepository.findById(id3)).thenReturn(Optional.empty());

        // when
        FireState result1 = validator.validateFireStateExists(id1);
        FireState result2 = validator.validateFireStateExists(id2);

        // then
        assertThat(result1.getId()).isEqualTo(id1);
        assertThat(result2.getId()).isEqualTo(id2);

        assertThatThrownBy(() -> validator.validateFireStateExists(id3))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FIRE_STATE_NOT_FOUND);
    }

    @Test
    @DisplayName("소방서 존재 검증 - 다양한 ID 값")
    void validateFireStateExists_variousIds() {
        // given
        Integer[] ids = {1, 10, 100, 999};

        for (Integer id : ids) {
            FireState state = FireState.builder().id(id).build();
            when(fireStateRepository.findById(id)).thenReturn(Optional.of(state));
        }

        // when & then
        for (Integer id : ids) {
            FireState result = validator.validateFireStateExists(id);
            assertThat(result.getId()).isEqualTo(id);
        }

        verify(fireStateRepository, times(ids.length)).findById(anyInt());
    }

    @Test
    @DisplayName("소방서 존재 검증 - Repository 호출 확인")
    void validateFireStateExists_verifyRepositoryCall() {
        // given
        when(fireStateRepository.findById(fireStateId))
                .thenReturn(Optional.of(fireState));

        // when
        validator.validateFireStateExists(fireStateId);

        // then
        verify(fireStateRepository, times(1)).findById(fireStateId);
    }
}
