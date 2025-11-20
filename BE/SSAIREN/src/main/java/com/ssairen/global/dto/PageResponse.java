package com.ssairen.global.dto;

import lombok.*;

import java.util.List;

/**
 * 페이지네이션 응답 DTO
 * @param <T> 페이지네이션할 데이터 타입
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> content;          // 현재 페이지의 데이터 목록
    private int page;                 // 현재 페이지 번호 (0부터 시작)
    private int size;                 // 페이지당 데이터 개수
    private long totalElements;       // 전체 데이터 개수
    private int totalPages;           // 전체 페이지 수

    /**
     * PageResponse 생성 팩토리 메서드
     *
     * @param content 현재 페이지 데이터
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
