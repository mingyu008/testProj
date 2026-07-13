package com.example.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class JsonStructureValidator {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON 데이터에 선별된 필수 필드들이 누락 없이 정확히 포함되어 있는지 검증
     */
    public static void validateTargetStructure(String jsonString, Class<?> dtoClass) {
        try {
            // 1. DTO 클래스에서 @CheckTarget 어노테이션이 붙은 '선별 필드'만 수집
            Set<String> targetFieldNames = new HashSet<>();
            for (Field field : dtoClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(CheckTarget.class)) {
                    targetFieldNames.add(field.getName());
                }
            }

            // 만약 DTO에 선별된 타겟이 하나도 없다면 검증을 건너뜁니다.
            if (targetFieldNames.isEmpty()) {
                return;
            }

            // 2. 들어온 JSON을 트리 노드로 파싱 후 실제 포함된 Key들을 수집
            JsonNode rootNode = objectMapper.readTree(jsonString);
            Set<String> jsonFieldNames = new HashSet<>();
            Iterator<String> fieldNames = rootNode.fieldNames();
            while (fieldNames.hasNext()) {
                jsonFieldNames.add(fieldNames.next());
            }

            // 3. 선별된 필수 타겟 필드들이 JSON에 다 들어있는지 대조 (누락 검사)
            Set<String> missingFields = new HashSet<>();
            for (String targetField : targetFieldNames) {
                if (!jsonFieldNames.contains(targetField)) {
                    missingFields.add(targetField);
                }
            }

            // 누락된 선별 필드가 있다면 예외 발생
            if (!missingFields.isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("필수 JSON 필드가 누락되었습니다: %s", missingFields)
                );
            }

        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalArgumentException("JSON 구조 선별 분석 중 오류가 발생했습니다.", e);
        }
    }
}