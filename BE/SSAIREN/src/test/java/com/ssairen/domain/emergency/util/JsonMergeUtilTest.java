package com.ssairen.domain.emergency.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonMergeUtilTest {

    private JsonMergeUtil util;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        util = new JsonMergeUtil(objectMapper);
    }

    @Test
    @DisplayName("기본 병합 - 새 값으로 덮어쓰기")
    void merge_basicOverwrite() throws Exception {
        // given
        String existingJson = "{\"name\": \"홍길동\", \"age\": 30}";
        String updateJson = "{\"age\": 35, \"city\": \"서울\"}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.merge(existing, update);

        // then
        assertThat(result.get("name").asText()).isEqualTo("홍길동");
        assertThat(result.get("age").asInt()).isEqualTo(35); // 덮어씀
        assertThat(result.get("city").asText()).isEqualTo("서울");
    }

    @Test
    @DisplayName("기본 병합 - 중첩 객체 병합")
    void merge_nestedObjects() throws Exception {
        // given
        String existingJson = "{\"user\": {\"name\": \"홍길동\", \"age\": 30}}";
        String updateJson = "{\"user\": {\"age\": 35, \"email\": \"hong@test.com\"}}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.merge(existing, update);

        // then
        assertThat(result.get("user").get("name").asText()).isEqualTo("홍길동");
        assertThat(result.get("user").get("age").asInt()).isEqualTo(35);
        assertThat(result.get("user").get("email").asText()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("기본 병합 - null 기존 데이터")
    void merge_nullExisting() throws Exception {
        // given
        JsonNode existing = null;
        String updateJson = "{\"name\": \"홍길동\"}";
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.merge(existing, update);

        // then
        assertThat(result.get("name").asText()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("기본 병합 - null 업데이트 데이터")
    void merge_nullUpdate() throws Exception {
        // given
        String existingJson = "{\"name\": \"홍길동\"}";
        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = null;

        // when
        JsonNode result = util.merge(existing, update);

        // then
        assertThat(result.get("name").asText()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("기존 값 우선 병합 - 기존 값이 있으면 유지")
    void mergePreservingExisting_keepExisting() throws Exception {
        // given
        String existingJson = "{\"name\": \"홍길동\", \"age\": 30}";
        String updateJson = "{\"name\": \"김철수\", \"age\": 35}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.mergePreservingExisting(existing, update);

        // then
        assertThat(result.get("name").asText()).isEqualTo("홍길동"); // 기존 값 유지
        assertThat(result.get("age").asInt()).isEqualTo(30); // 기존 값 유지
    }

    @Test
    @DisplayName("기존 값 우선 병합 - 기존 값이 null이면 새 값 사용")
    void mergePreservingExisting_fillNulls() throws Exception {
        // given
        String existingJson = "{\"name\": null, \"age\": 30}";
        String updateJson = "{\"name\": \"홍길동\", \"age\": 35}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.mergePreservingExisting(existing, update);

        // then
        assertThat(result.get("name").asText()).isEqualTo("홍길동"); // null이었으므로 새 값
        assertThat(result.get("age").asInt()).isEqualTo(30); // 기존 값 유지
    }

    @Test
    @DisplayName("기존 값 우선 병합 - 기존 값이 빈 문자열이면 새 값 사용")
    void mergePreservingExisting_fillEmptyStrings() throws Exception {
        // given
        String existingJson = "{\"name\": \"\", \"city\": \"서울\"}";
        String updateJson = "{\"name\": \"홍길동\", \"city\": \"부산\"}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.mergePreservingExisting(existing, update);

        // then
        assertThat(result.get("name").asText()).isEqualTo("홍길동"); // 빈 문자열이었으므로 새 값
        assertThat(result.get("city").asText()).isEqualTo("서울"); // 기존 값 유지
    }

    @Test
    @DisplayName("기존 값 우선 병합 - 중첩 객체")
    void mergePreservingExisting_nestedObjects() throws Exception {
        // given
        String existingJson = "{\"user\": {\"name\": \"홍길동\", \"age\": null}}";
        String updateJson = "{\"user\": {\"name\": \"김철수\", \"age\": 30}}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.mergePreservingExisting(existing, update);

        // then
        assertThat(result.get("user").get("name").asText()).isEqualTo("홍길동"); // 기존 값 유지
        assertThat(result.get("user").get("age").asInt()).isEqualTo(30); // null이었으므로 새 값
    }

    @Test
    @DisplayName("기본 병합 - 깊은 중첩 구조")
    void merge_deeplyNested() throws Exception {
        // given
        String existingJson = "{\"a\": {\"b\": {\"c\": {\"d\": \"value1\"}}}}";
        String updateJson = "{\"a\": {\"b\": {\"c\": {\"e\": \"value2\"}}}}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.merge(existing, update);

        // then
        assertThat(result.get("a").get("b").get("c").get("d").asText()).isEqualTo("value1");
        assertThat(result.get("a").get("b").get("c").get("e").asText()).isEqualTo("value2");
    }

    @Test
    @DisplayName("기본 병합 - 배열 값 덮어쓰기")
    void merge_arrayOverwrite() throws Exception {
        // given
        String existingJson = "{\"items\": [1, 2, 3]}";
        String updateJson = "{\"items\": [4, 5]}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.merge(existing, update);

        // then
        assertThat(result.get("items").isArray()).isTrue();
        assertThat(result.get("items").size()).isEqualTo(2);
        assertThat(result.get("items").get(0).asInt()).isEqualTo(4);
    }

    @Test
    @DisplayName("기본 병합 - 복잡한 구조")
    void merge_complexStructure() throws Exception {
        // given
        String existingJson = """
            {
                "patient": {
                    "name": "홍길동",
                    "age": 30,
                    "address": {
                        "city": "서울",
                        "district": null
                    }
                },
                "diagnosis": "감기"
            }
            """;
        String updateJson = """
            {
                "patient": {
                    "age": 31,
                    "address": {
                        "district": "강남구",
                        "zipcode": "12345"
                    },
                    "phone": "010-1234-5678"
                },
                "treatment": "약 처방"
            }
            """;

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.merge(existing, update);

        // then
        assertThat(result.get("patient").get("name").asText()).isEqualTo("홍길동");
        assertThat(result.get("patient").get("age").asInt()).isEqualTo(31);
        assertThat(result.get("patient").get("phone").asText()).isEqualTo("010-1234-5678");
        assertThat(result.get("patient").get("address").get("city").asText()).isEqualTo("서울");
        assertThat(result.get("patient").get("address").get("district").asText()).isEqualTo("강남구");
        assertThat(result.get("patient").get("address").get("zipcode").asText()).isEqualTo("12345");
        assertThat(result.get("diagnosis").asText()).isEqualTo("감기");
        assertThat(result.get("treatment").asText()).isEqualTo("약 처방");
    }

    @Test
    @DisplayName("기존 값 우선 병합 - 빈 문자열과 공백 문자열")
    void mergePreservingExisting_emptyAndWhitespace() throws Exception {
        // given
        String existingJson = "{\"field1\": \"\", \"field2\": \"  \", \"field3\": \"value\"}";
        String updateJson = "{\"field1\": \"new1\", \"field2\": \"new2\", \"field3\": \"new3\"}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.mergePreservingExisting(existing, update);

        // then
        assertThat(result.get("field1").asText()).isEqualTo("new1"); // 빈 문자열
        assertThat(result.get("field2").asText()).isEqualTo("new2"); // 공백 문자열
        assertThat(result.get("field3").asText()).isEqualTo("value"); // 기존 값 유지
    }

    @Test
    @DisplayName("기본 병합 - 숫자, 불린, null 값")
    void merge_primitiveTypes() throws Exception {
        // given
        String existingJson = "{\"num\": 10, \"bool\": true, \"str\": \"test\"}";
        String updateJson = "{\"num\": 20, \"bool\": false, \"str\": null}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.merge(existing, update);

        // then
        assertThat(result.get("num").asInt()).isEqualTo(20);
        assertThat(result.get("bool").asBoolean()).isFalse();
        assertThat(result.get("str").isNull()).isTrue();
    }

    @Test
    @DisplayName("기존 값 우선 병합 - 새로운 필드 추가")
    void mergePreservingExisting_newFields() throws Exception {
        // given
        String existingJson = "{\"name\": \"홍길동\"}";
        String updateJson = "{\"name\": \"김철수\", \"age\": 30, \"city\": \"서울\"}";

        JsonNode existing = objectMapper.readTree(existingJson);
        JsonNode update = objectMapper.readTree(updateJson);

        // when
        JsonNode result = util.mergePreservingExisting(existing, update);

        // then
        assertThat(result.get("name").asText()).isEqualTo("홍길동"); // 기존 값 유지
        assertThat(result.get("age").asInt()).isEqualTo(30); // 새 필드 추가
        assertThat(result.get("city").asText()).isEqualTo("서울"); // 새 필드 추가
    }
}
