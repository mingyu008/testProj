package com.example.service;

import com.example.validator.LengthValidator;
import com.example.validator.ValidatableDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class CommonJsonProcessService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T extends ValidatableDto> T parseAndValidate(String jsonString, Class<T> clazz) {
        try {
            // 1. 런타임에 타겟 DTO의 빈 기본 인스턴스를 생성하여 명세 확보
            T emptyDtoInstance = clazz.getDeclaredConstructor().newInstance();
            LengthValidator spec = emptyDtoInstance.getValidatorSpecification();

            if (spec != null) {
                Set<String> requiredTargetFields = spec.getTargetFields();

                // 2. 수신된 JSON의 원본 Key 목록 파싱
                JsonNode rootNode = objectMapper.readTree(jsonString);
                List<String> jsonKeys = new ArrayList<>();
                Iterator<String> fieldNames = rootNode.fieldNames();
                while (fieldNames.hasNext()) {
                    jsonKeys.add(fieldNames.next());
                }

                // 3. 누락된 필수 라벨 역추적 대조
                List<String> missingLabels = new ArrayList<>();
                for (String requiredField : requiredTargetFields) {
                    if (!jsonKeys.contains(requiredField)) {
                        missingLabels.add(spec.getLabel(requiredField));
                    }
                }

                if (!missingLabels.isEmpty()) {
                    throw new IllegalArgumentException("필수 입력 항목이 누락되었습니다: " + missingLabels);
                }
            }

            // 4. 구조 대조 통과 시 맵핑 및 세부 값 규칙(길이, 바이트) 최종 검증
            T fullyPopulatedDto = objectMapper.readValue(jsonString, clazz);
            fullyPopulatedDto.validate();

            return fullyPopulatedDto;

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("올바르지 않은 JSON 형식입니다.", e);
        } catch (Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            }
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalStateException("DTO 검증 사양 처리 중 내부 시스템 오류가 발생했습니다.", e);
        }
    }
}