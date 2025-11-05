package com.ssairen.domain.hospital.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.hospital.dto.*;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.dto.PageResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 병원 API 스펙 정의
 * Swagger 문서화를 위한 인터페이스
 */
@Tag(name = "Hospital", description = "병원 이송 요청 관리 API")
public interface HospitalApi {

    /**
     * 병원 이송 요청 생성
     * - 구급대원이 여러 병원에 동시에 환자 이송 요청을 보냄
     * - 각 병원에 웹소켓으로 실시간 알림 전송
     */
    @Operation(
            summary = "병원 이송 요청 생성",
            description = "구급대원이 여러 병원에 환자 이송 요청을 전송합니다. 각 병원에 웹소켓으로 실시간 알림이 전송됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "요청 전송 성공",
                    content = @Content(schema = @Schema(implementation = HospitalSelectionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (구급일지 없음, 병원 정보 오류 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구급일지 또는 병원을 찾을 수 없음"
            )
    })
    @ApiInternalServerError
    ResponseEntity<ApiResponse<HospitalSelectionResponse>> createHospitalSelectionRequest(
            @Parameter(description = "병원 이송 요청 정보 (구급일지 ID, 병원 이름 목록)", required = true)
            @Valid @RequestBody HospitalSelectionRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal
    );

    /**
     * 병원 이송 요청에 응답
     * - 병원이 환자 수용 요청에 대해 응답 (수용/거절/전화요망)
     * - ACCEPTED 시 같은 구급일지의 다른 병원 요청은 자동으로 COMPLETED 처리
     */
    @Operation(
            summary = "병원 이송 요청에 응답",
            description = "병원이 환자 수용 요청에 대해 응답합니다. 수용(ACCEPTED), 거절(REJECTED), 전화요망(CALLREQUEST) 중 하나를 선택할 수 있습니다. " +
                    "병원이 수용(ACCEPTED)하면 같은 구급일지의 다른 병원 요청은 자동으로 완료(COMPLETED) 처리됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "응답 처리 성공",
                    content = @Content(schema = @Schema(implementation = HospitalResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "이미 처리된 요청"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 병원의 요청에 응답 시도)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "병원 선택 요청을 찾을 수 없음"
            )
    })
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<ApiResponse<HospitalResponseDto>> respondToRequest(
            @Parameter(description = "병원 선택 ID", required = true, example = "48")
            @PathVariable @Positive(message = "병원 선택 ID는 양의 정수여야 합니다.") Integer hospitalSelectionId,
            @Parameter(description = "병원 응답 정보 (status: ACCEPTED, REJECTED, CALLREQUEST)", required = true)
            @Valid @RequestBody HospitalResponseRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    /**
     * 병원의 PENDING 상태인 요청 목록 조회
     * - 병원이 아직 응답하지 않은 환자 이송 요청 목록 조회
     * - 각 요청에는 환자 정보(PatientInfo) 포함
     */
    @Operation(
            summary = "병원의 PENDING 요청 목록 조회",
            description = "병원이 아직 응답하지 않은(PENDING) 환자 이송 요청 목록을 조회합니다. " +
                    "각 요청에는 환자의 바이탈 사인, 나이, 성별 등의 정보가 포함됩니다. " +
                    "웹소켓 연결이 끊겼거나 화면을 새로고침할 때 사용합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = HospitalRequestMessage.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 병원의 요청 목록 조회 시도)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "병원을 찾을 수 없음"
            )
    })
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<ApiResponse<List<HospitalRequestMessage>>> getPendingRequests(
            @Parameter(description = "병원 ID", required = true, example = "5")
            @PathVariable @Positive(message = "병원 ID는 양의 정수여야 합니다.") Integer hospitalId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    /**
     * 병원이 수용한 환자 목록 조회 (페이지네이션)
     * - ACCEPTED (내원 대기중) 또는 ARRIVED (내원 완료) 상태의 환자 목록
     * - 각 환자의 기본 정보와 내원 상태 포함
     * - 페이지네이션 및 상태 필터 지원
     */
    @Operation(
            summary = "병원이 수용한 환자 목록 조회 (페이지네이션)",
            description = "병원이 수용(ACCEPTED)하거나 내원 완료(ARRIVED)된 환자 목록을 페이지네이션하여 조회합니다. " +
                    "각 환자의 성별, 나이, 주호소, 의식상태 등의 정보와 함께 내원 상태를 확인할 수 있습니다.\n\n" +
                    "**status 파라미터 설명:**\n" +
                    "- `all`: ACCEPTED(내원 대기) + ARRIVED(내원 완료) 모두 조회 (기본값)\n" +
                    "- `accepted`: ACCEPTED(내원 대기) 상태만 조회\n\n" +
                    "**페이지네이션 예시:**\n" +
                    "- 1페이지 (1-10번): `page=0&size=10`\n" +
                    "- 2페이지 (11-20번): `page=1&size=10`\n" +
                    "- 3페이지 (21-30번): `page=2&size=10`"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 병원의 환자 목록 조회 시도)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "병원을 찾을 수 없음"
            )
    })
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<ApiResponse<PageResponse<AcceptedPatientDto>>> getAcceptedPatients(
            @Parameter(description = "병원 ID", required = true, example = "2")
            @PathVariable @Positive(message = "병원 ID는 양의 정수여야 합니다.") Integer hospitalId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.") int page,
            @Parameter(description = "페이지당 데이터 개수", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.") @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.") int size,
            @Parameter(description = "상태 필터 (all: 전체, accepted: 내원 대기만)", example = "all")
            @RequestParam(defaultValue = "all") String status,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    /**
     * 병원이 수용한 환자의 상세 정보 조회
     * - 환자의 모든 바이탈 사인, 과거력 등 전체 정보 조회
     * - 병원이 ACCEPTED 또는 ARRIVED 상태인 환자만 조회 가능
     */
    @Operation(
            summary = "수용한 환자 상세 정보 조회",
            description = "병원이 수용한 환자의 상세 정보를 조회합니다. " +
                    "환자의 바이탈 사인(심박수, 혈압, 산소포화도 등), 과거력, 발병 시간 등 모든 정보를 확인할 수 있습니다. " +
                    "ACCEPTED(내원 대기) 또는 ARRIVED(내원 완료) 상태인 환자만 조회 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PatientInfoDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (수용하지 않은 환자 또는 다른 병원의 환자)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "환자 정보를 찾을 수 없음"
            )
    })
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<ApiResponse<PatientInfoDto>> getPatientDetail(
            @Parameter(description = "병원 ID", required = true, example = "5")
            @PathVariable @Positive(message = "병원 ID는 양의 정수여야 합니다.") Integer hospitalId,
            @Parameter(description = "구급일지 ID (환자 식별자)", required = true, example = "1")
            @PathVariable @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    /**
     * 병원이 수용한 환자의 정보 수정
     * - 환자의 바이탈 사인, 의식상태, 과거력 등 정보 수정
     * - 병원이 ACCEPTED 또는 ARRIVED 상태인 환자만 수정 가능
     */
    @Operation(
            summary = "수용한 환자 정보 수정",
            description = "병원이 수용한 환자의 정보를 수정합니다. " +
                    "환자의 성별, 나이, 바이탈 사인(심박수, 혈압, 산소포화도 등), 의식상태, 과거력 등을 수정할 수 있습니다. " +
                    "ACCEPTED(내원 대기) 또는 ARRIVED(내원 완료) 상태인 환자만 수정 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = PatientInfoDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 입력값)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (수용하지 않은 환자 또는 다른 병원의 환자)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "환자 정보를 찾을 수 없음"
            )
    })
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<ApiResponse<PatientInfoDto>> updatePatientInfo(
            @Parameter(description = "병원 ID", required = true, example = "5")
            @PathVariable @Positive(message = "병원 ID는 양의 정수여야 합니다.") Integer hospitalId,
            @Parameter(description = "구급일지 ID (환자 식별자)", required = true, example = "1")
            @PathVariable @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @Parameter(description = "환자 정보 수정 요청", required = true)
            @Valid @RequestBody UpdatePatientInfoRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    /**
     * 환자 내원 완료 처리
     * - 수용한 환자(ACCEPTED)가 실제로 병원에 도착했을 때 상태를 내원 완료(ARRIVED)로 변경
     * - ACCEPTED 상태인 환자만 처리 가능
     */
    @Operation(
            summary = "환자 내원 완료 처리",
            description = "수용한 환자가 실제로 병원에 도착했을 때 상태를 ARRIVED로 변경합니다. " +
                    "ACCEPTED(내원 대기) 상태인 환자만 처리 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내원 완료 처리 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 병원의 환자)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "수용 대기 중인 환자를 찾을 수 없음 (ACCEPTED 상태가 아님)"
            )
    })
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<ApiResponse<Void>> markPatientAsArrived(
            @Parameter(description = "병원 ID", required = true, example = "5")
            @PathVariable @Positive(message = "병원 ID는 양의 정수여야 합니다.") Integer hospitalId,
            @Parameter(description = "구급일지 ID (환자 식별자)", required = true, example = "1")
            @PathVariable @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    /**
     * 구급일지별 병원 선택 상태 조회 (구급대원용)
     * - 구급대원이 이송 요청을 보낸 병원들의 응답 상태를 확인
     * - 각 병원의 ID, 이름, 상태(PENDING, ACCEPTED, REJECTED 등) 반환
     */
    @Operation(
            summary = "구급일지별 병원 선택 상태 조회",
            description = "구급대원이 특정 구급일지에 대해 이송 요청을 보낸 병원들의 응답 상태를 조회합니다. " +
                    "각 병원의 ID, 공식 명칭, 현재 상태(PENDING, ACCEPTED, REJECTED, CALLREQUEST, COMPLETED)를 확인할 수 있습니다. " +
                    "실시간으로 병원들의 응답 상태를 확인하여 적절한 병원을 선택할 수 있습니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = HospitalSelectionStatusResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구급일지를 찾을 수 없음"
            )
    })
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<ApiResponse<HospitalSelectionStatusResponse>> getHospitalSelectionStatus(
            @Parameter(description = "구급일지 ID", required = true, example = "1")
            @PathVariable("emergency_report_id") @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal
    );

    /**
     * AI 기반 병원 추천 및 이송 요청
     * - 환자 정보와 위치 정보를 기반으로 AI가 최적의 병원을 추천
     * - 추천된 병원들에게 자동으로 웹소켓으로 이송 요청 전송
     * - AI 추론 정보와 병원 선택 결과를 함께 반환
     */
    @Operation(
            summary = "AI 기반 병원 추천 및 이송 요청",
            description = "환자의 상태 정보와 현재 위치를 기반으로 AI가 최적의 병원을 추천하고, " +
                    "추천된 병원들에게 자동으로 웹소켓을 통해 이송 요청을 전송합니다. " +
                    "구급일지 ID, 위도, 경도, 검색 반경을 입력하면 AI가 환자 정보를 분석하여 적합한 병원 목록을 추천하고, " +
                    "해당 병원들에게 실시간으로 알림을 전송합니다. " +
                    "응답에는 AI의 추론 과정(GPT reasoning), 추천 병원 목록, 생성된 병원 이송 요청 정보가 포함됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 및 요청 전송 성공",
                    content = @Content(schema = @Schema(implementation = AiHospitalRecommendationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 입력값)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구급일지를 찾을 수 없음 또는 추천된 병원을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "AI 서버 연결 실패"
            )
    })
    @ApiInternalServerError
    ResponseEntity<ApiResponse<AiHospitalRecommendationResponse>> getAiHospitalRecommendation(
            @Parameter(description = "AI 병원 추천 요청 정보 (구급일지 ID, 위도, 경도, 반경)", required = true)
            @Valid @RequestBody HospitalAiRecommendationRequest request
    );
}
