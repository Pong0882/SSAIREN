package com.ssairen.global.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    @DisplayName("PageResponse 생성 - of 메서드")
    void of_basicPageResponse() {
        // given
        List<String> content = Arrays.asList("item1", "item2", "item3");
        int page = 0;
        int size = 10;
        long totalElements = 25;

        // when
        PageResponse<String> response = PageResponse.of(content, page, size, totalElements);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getTotalPages()).isEqualTo(3); // ceil(25/10) = 3
    }

    @Test
    @DisplayName("총 페이지 수 계산 - 정확히 나누어떨어지는 경우")
    void totalPages_exactDivision() {
        // given
        List<String> content = Arrays.asList("item1", "item2");
        int size = 10;
        long totalElements = 30; // 30 / 10 = 3

        // when
        PageResponse<String> response = PageResponse.of(content, 0, size, totalElements);

        // then
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("총 페이지 수 계산 - 나누어떨어지지 않는 경우")
    void totalPages_withRemainder() {
        // given
        List<String> content = Collections.emptyList();
        int size = 10;
        long totalElements = 25; // ceil(25/10) = 3

        // when
        PageResponse<String> response = PageResponse.of(content, 0, size, totalElements);

        // then
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("첫 번째 페이지")
    void firstPage() {
        // given
        List<Integer> content = Arrays.asList(1, 2, 3, 4, 5);

        // when
        PageResponse<Integer> response = PageResponse.of(content, 0, 5, 15);

        // then
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getContent()).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("마지막 페이지")
    void lastPage() {
        // given
        List<Integer> content = Arrays.asList(11, 12, 13);

        // when
        PageResponse<Integer> response = PageResponse.of(content, 2, 5, 13);

        // then
        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("빈 페이지")
    void emptyPage() {
        // given
        List<String> content = Collections.emptyList();

        // when
        PageResponse<String> response = PageResponse.of(content, 0, 10, 0);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("단일 항목 페이지")
    void singleItemPage() {
        // given
        List<String> content = Arrays.asList("single");

        // when
        PageResponse<String> response = PageResponse.of(content, 0, 10, 1);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("다양한 데이터 타입 - Integer")
    void variousDataTypes_integer() {
        // given
        List<Integer> content = Arrays.asList(1, 2, 3);

        // when
        PageResponse<Integer> response = PageResponse.of(content, 0, 10, 30);

        // then
        assertThat(response.getContent()).containsExactly(1, 2, 3);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("다양한 데이터 타입 - Custom 객체")
    void variousDataTypes_customObject() {
        // given
        ErrorResponse error1 = ErrorResponse.of("ERR1", "에러1");
        ErrorResponse error2 = ErrorResponse.of("ERR2", "에러2");
        List<ErrorResponse> content = Arrays.asList(error1, error2);

        // when
        PageResponse<ErrorResponse> response = PageResponse.of(content, 1, 2, 10);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(5); // ceil(10/2) = 5
    }

    @Test
    @DisplayName("페이지 크기가 1인 경우")
    void pageSize_one() {
        // given
        List<String> content = Arrays.asList("item");

        // when
        PageResponse<String> response = PageResponse.of(content, 5, 1, 10);

        // then
        assertThat(response.getSize()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(10); // 10 / 1 = 10
    }

    @Test
    @DisplayName("큰 페이지 크기")
    void largePageSize() {
        // given
        List<String> content = Arrays.asList("item1", "item2");

        // when
        PageResponse<String> response = PageResponse.of(content, 0, 1000, 2);

        // then
        assertThat(response.getSize()).isEqualTo(1000);
        assertThat(response.getTotalPages()).isEqualTo(1); // ceil(2/1000) = 1
    }

    @Test
    @DisplayName("Builder 패턴 사용")
    void builder_pattern() {
        // given
        List<String> content = Arrays.asList("a", "b", "c");

        // when
        PageResponse<String> response = PageResponse.<String>builder()
                .content(content)
                .page(2)
                .size(20)
                .totalElements(100)
                .totalPages(5)
                .build();

        // then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(100);
        assertThat(response.getTotalPages()).isEqualTo(5);
    }

    @Test
    @DisplayName("Setter 사용")
    void setter_usage() {
        // given
        PageResponse<String> response = new PageResponse<>();

        // when
        response.setContent(Arrays.asList("item"));
        response.setPage(1);
        response.setSize(10);
        response.setTotalElements(50);
        response.setTotalPages(5);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(50);
        assertThat(response.getTotalPages()).isEqualTo(5);
    }

    @Test
    @DisplayName("totalPages 계산 - 다양한 케이스")
    void totalPages_variousCases() {
        // 0 elements
        PageResponse<String> r1 = PageResponse.of(Collections.emptyList(), 0, 10, 0);
        assertThat(r1.getTotalPages()).isEqualTo(0);

        // 1 element, size 10
        PageResponse<String> r2 = PageResponse.of(Arrays.asList("a"), 0, 10, 1);
        assertThat(r2.getTotalPages()).isEqualTo(1);

        // 10 elements, size 10
        PageResponse<String> r3 = PageResponse.of(Collections.emptyList(), 0, 10, 10);
        assertThat(r3.getTotalPages()).isEqualTo(1);

        // 11 elements, size 10
        PageResponse<String> r4 = PageResponse.of(Collections.emptyList(), 0, 10, 11);
        assertThat(r4.getTotalPages()).isEqualTo(2);

        // 99 elements, size 10
        PageResponse<String> r5 = PageResponse.of(Collections.emptyList(), 0, 10, 99);
        assertThat(r5.getTotalPages()).isEqualTo(10);

        // 100 elements, size 10
        PageResponse<String> r6 = PageResponse.of(Collections.emptyList(), 0, 10, 100);
        assertThat(r6.getTotalPages()).isEqualTo(10);
    }

    @Test
    @DisplayName("중간 페이지")
    void middlePage() {
        // given
        List<String> content = Arrays.asList("item21", "item22", "item23");

        // when
        PageResponse<String> response = PageResponse.of(content, 2, 10, 35);

        // then
        assertThat(response.getPage()).isEqualTo(2); // 3번째 페이지 (0-indexed)
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getTotalPages()).isEqualTo(4); // ceil(35/10) = 4
    }
}
