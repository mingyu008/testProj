package com.example.service;

import com.example.dto.OrderRequestDto;
import com.example.domain.Order;
import com.example.mapper.OrderMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Lombok이 매퍼를 DI(의존성 주입) 받기 위한 생성자를 자동 생성합니다.
public class OrderProcessService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderMapper orderMapper;

    /**
     * 외부 JSON 문자열을 받아 파싱, 정합성 검증 후 도메인 객체로 변환하여 반환
     */
    public Order processIncomingOrderJson(String jsonString) {
        try {
            // Step 1: JSON 파싱 (Lombok 기본생성자 기반 작동)
            OrderRequestDto dto = objectMapper.readValue(jsonString, OrderRequestDto.class);

            // Step 2: 정합성 검증 (필드 20개 이상도 수용하는 빌더 방식)
            dto.validate();

            // Step 3: MapStruct를 이용해 안전하게 내부 도메인 객체로 매핑 후 반환
            return orderMapper.toDomain(dto);

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("올바르지 않은 JSON 형식입니다.", e);
        }
    }
}