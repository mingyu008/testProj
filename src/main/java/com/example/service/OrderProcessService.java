package com.example.service;

import com.example.dto.OrderRequestDto;
import com.example.domain.Order;
import com.example.mapper.OrderMapper;
import com.example.validator.LengthValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderProcessService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderMapper orderMapper;

    public Order processIncomingOrderJson(String jsonString) {
        try {
            // 1단계: 빈 DTO 사양 객체로부터 우리가 add 해둔 검증 타겟 필드셋(Set)을 추출합니다.
            LengthValidator spec = new OrderRequestDto().getValidatorSpecification();
            Set<String> requiredTargetFields = spec.getTargetFields();

            // 2단계: 들어온 JSON 문자열의 Key 목록을 읽어옵니다.
            JsonNode rootNode = objectMapper.readTree(jsonString);
            List<String> jsonKeys = new ArrayList<>();
            Iterator<String> fieldNames = rootNode.fieldNames();
            while (fieldNames.hasNext()) {
                jsonKeys.add(fieldNames.next());
            }

            // 3단계: 빌더에 add 된 필드 중 JSON에 누락된 필드가 있는지 대조합니다.
            List<String> missingLabels = new ArrayList<>();
            for (String requiredField : requiredTargetFields) {
                if (!jsonKeys.contains(requiredField)) {
                    // 유저가 이해하기 쉽도록 한글 라벨을 에러 메시지에 담아줍니다.
                    missingLabels.add(spec.getLabel(requiredField));
                }
            }

            // 누락 항목이 있다면 파싱을 중단하고 예외를 던집니다.
            if (!missingLabels.isEmpty()) {
                throw new IllegalArgumentException("필수 입력 항목이 JSON 데이터에서 누락되었습니다: " + missingLabels);
            }

            // 4단계: 구조 검증을 통과했으므로 파싱 및 세부 값(길이/바이트) 정합성 체크 진행
            OrderRequestDto dto = objectMapper.readValue(jsonString, OrderRequestDto.class);
            dto.validate();

            return orderMapper.toDomain(dto);

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("올바르지 않은 JSON 형식입니다.", e);
        }
    }
}