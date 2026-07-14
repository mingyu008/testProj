package com.step.service;

import com.step.domain.ProductStep;
import com.step.dto.ProductRequestStepDto;
import com.step.mapper.ProductStepMapper;
import com.step.validator.ValidationStepResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductProcessStepService {

    private final CommonJsonProcessStepService commonJsonProcessService;
    private final ProductStepMapper productMapper;

    /**
     * 수신된 JSON을 안전하게 처리하고 전체 결과를 모아 보고서 형태로 수집하여 반환
     */
    public ValidationStepResult<ProductStep> getProcessedProductReport(String jsonString) {
        // 제네릭 공통 서비스에 DTO 클래스 타입과 MapStruct 매핑 기능(람다)을 주입하여 호출
        return commonJsonProcessService.parseAndValidateSafely(
                jsonString,
                ProductRequestStepDto.class,
                productMapper::toDomain
        );
    }
}
