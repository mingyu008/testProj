package com.example.mapper;

import com.example.dto.ProductRequestDto;
import com.example.domain.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ProductMapper {
    Product toDomain(ProductRequestDto dto);
}