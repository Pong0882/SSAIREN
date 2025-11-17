package com.ssairen.domain.emergency.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * JSON 데이터 병합 유틸리티
 * 기존 JSON 데이터와 새로운 JSON 데이터를 깊은 병합(deep merge)하여 반환
 */
@Slf4j
@Component
public class JsonMergeUtil {

    private final ObjectMapper objectMapper;

    public JsonMergeUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 두 개의 JsonNode를 깊은 병합(deep merge)하여 반환
     *
     * @param existingData 기존 데이터
     * @param updateData 업데이트할 데이터
     * @return 병합된 JsonNode
     */
    public JsonNode merge(JsonNode existingData, JsonNode updateData) {
        return merge(existingData, updateData, false);
    }

    /**
     * 기존 값을 우선하는 병합 (기존 값이 비어있을 때만 새 값으로 채움)
     * - 기존 값이 null이거나 빈 문자열이면 새 값으로 채움
     * - 기존 값이 이미 있으면 기존 값 유지
     *
     * @param existingData 기존 데이터
     * @param updateData 업데이트할 데이터
     * @return 병합된 JsonNode
     */
    public JsonNode mergePreservingExisting(JsonNode existingData, JsonNode updateData) {
        return merge(existingData, updateData, true);
    }

    /**
     * 두 개의 JsonNode를 깊은 병합(deep merge)하여 반환 (통합 메서드)
     *
     * @param existingData 기존 데이터
     * @param updateData 업데이트할 데이터
     * @param preserveExisting true인 경우 기존 값 우선, false인 경우 새 값으로 덮어씀
     * @return 병합된 JsonNode
     */
    private JsonNode merge(JsonNode existingData, JsonNode updateData, boolean preserveExisting) {
        if (existingData == null || existingData.isNull()) {
            return updateData;
        }
        if (updateData == null || updateData.isNull()) {
            return existingData;
        }

        // 복사본 생성
        JsonNode mergedNode = existingData.deepCopy();

        // 재귀적으로 병합
        return mergeRecursive(mergedNode, updateData, preserveExisting);
    }


    /**
     * JsonNode 값이 비어있는지 확인
     * - null
     * - 빈 문자열 ("")
     *
     * @param node 확인할 노드
     * @return 비어있으면 true
     */
    private boolean isEmpty(JsonNode node) {
        if (node == null || node.isNull()) {
            return true;
        }
        if (node.isTextual()) {
            String text = node.asText();
            return text == null || text.trim().isEmpty();
        }
        return false;
    }

    /**
     * 재귀적으로 JSON을 병합 (통합 메서드)
     *
     * @param mainNode 기존 노드
     * @param updateNode 업데이트할 노드
     * @param preserveExisting true인 경우 기존 값 우선, false인 경우 새 값으로 덮어씀
     * @return 병합된 노드
     */
    private JsonNode mergeRecursive(JsonNode mainNode, JsonNode updateNode, boolean preserveExisting) {
        if (updateNode == null || updateNode.isNull()) {
            return mainNode;
        }

        // updateNode가 객체인 경우
        if (updateNode.isObject() && mainNode.isObject()) {
            ObjectNode mainObjectNode = (ObjectNode) mainNode;
            ObjectNode updateObjectNode = (ObjectNode) updateNode;

            Iterator<Map.Entry<String, JsonNode>> fields = updateObjectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode updateValue = entry.getValue();

                if (mainObjectNode.has(fieldName)) {
                    JsonNode existingValue = mainObjectNode.get(fieldName);

                    if (preserveExisting) {
                        // 기존 값 우선 모드: 기존 값이 비어있는지 확인
                        if (isEmpty(existingValue)) {
                            // 기존 값이 비어있으면 새 값으로 대체
                            mainObjectNode.set(fieldName, updateValue);
                        } else if (existingValue.isObject() && updateValue.isObject()) {
                            // 둘 다 객체이고 기존 값이 비어있지 않으면 재귀적으로 병합
                            JsonNode mergedChild = mergeRecursive(existingValue, updateValue, true);
                            mainObjectNode.set(fieldName, mergedChild);
                        }
                        // 그 외의 경우 기존 값 유지 (아무것도 하지 않음)
                    } else {
                        // 덮어쓰기 모드: 둘 다 객체인 경우 재귀적으로 병합
                        if (existingValue.isObject() && updateValue.isObject()) {
                            JsonNode mergedChild = mergeRecursive(existingValue, updateValue, false);
                            mainObjectNode.set(fieldName, mergedChild);
                        } else {
                            // 그 외의 경우 새로운 값으로 대체
                            mainObjectNode.set(fieldName, updateValue);
                        }
                    }
                } else {
                    // 기존에 없던 필드는 새 값 추가
                    mainObjectNode.set(fieldName, updateValue);
                }
            }
            return mainObjectNode;
        }

        // 객체가 아닌 경우
        if (preserveExisting) {
            // 기존 값 우선 모드: 기존 값 유지
            return mainNode;
        } else {
            // 덮어쓰기 모드: 새로운 값으로 대체
            return updateNode;
        }
    }
}
