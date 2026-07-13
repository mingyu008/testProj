package com.example.service;

import com.example.dto.ProductRequestDto;
import com.example.domain.Product;
import com.example.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductProcessService {

    private final CommonJsonProcessService commonJsonProcessService;
    private final ProductMapper productMapper;

    /**
     * 외부 상품 연동 JSON 메시지를 처리하는 비즈니스 진입점
     */
    public Product processIncomingProductJson(String jsonString) {
        // 공통 제네릭 서비스를 통해 'ProductRequestDto' 규격 검증 및 역직렬화 수행
        ProductRequestDto dto = commonJsonProcessService.parseAndValidate(jsonString, ProductRequestDto.class);

        // 도메인 객체로 변환하여 비즈니스 레이어로 포워딩
        return productMapper.toDomain(dto);
    }
}