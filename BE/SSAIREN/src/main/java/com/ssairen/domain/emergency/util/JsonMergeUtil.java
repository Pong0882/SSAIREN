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
        if (existingData == null) {
            return updateData;
        }
        if (updateData == null) {
            return existingData;
        }

        // 복사본 생성
        JsonNode mergedNode = existingData.deepCopy();

        // 재귀적으로 병합
        return mergeRecursive(mergedNode, updateData);
    }

    /**
     * 재귀적으로 JSON을 병합
     *
     * @param mainNode 기존 노드
     * @param updateNode 업데이트할 노드
     * @return 병합된 노드
     */
    private JsonNode mergeRecursive(JsonNode mainNode, JsonNode updateNode) {
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

                // mainNode에 해당 필드가 있고 둘 다 객체인 경우 재귀적으로 병합
                if (mainObjectNode.has(fieldName)
                    && mainObjectNode.get(fieldName).isObject()
                    && updateValue.isObject()) {
                    JsonNode mergedChild = mergeRecursive(
                        mainObjectNode.get(fieldName),
                        updateValue
                    );
                    mainObjectNode.set(fieldName, mergedChild);
                } else {
                    // 그 외의 경우 새로운 값으로 대체
                    mainObjectNode.set(fieldName, updateValue);
                }
            }
            return mainObjectNode;
        }

        // 객체가 아닌 경우 (배열, 원시값 등) 새로운 값으로 대체
        return updateNode;
    }
}
