package com.example.mapper;

import com.example.dto.OrderRequestDto;
import com.example.domain.Order;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

// 필드명이 일치하지 않을 때 경고를 날리도록 unmappedTargetPolicy 설정 (선택)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface OrderMapper {

    // DTO에서 순수 도메인 엔티티 객체로 변환
    Order toDomain(OrderRequestDto dto);
}