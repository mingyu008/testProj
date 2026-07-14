package com.step.mapper;

import com.step.domain.ProductStep;
import com.step.dto.ProductRequestStepDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ProductStepMapper {
    ProductStep toDomain(ProductRequestStepDto dto);
}