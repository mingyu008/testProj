package com.step.service;

import com.step.domain.ProductStep;
import com.step.validator.ValidationStepResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ProductProcessStepService.class)
class ProductProcessStepServiceTest {

    @Autowired
    private ProductProcessStepService productProcessService;

    @Test
    @DisplayName("성공 시나리오: 예외 없이 검증에 통과하고, toDomain으로 가공된 최종 도메인 객체가 래퍼에 안전하게 반환된다")
    void getProcessedProductReport_Success() {
        // Given: 유효한 데이터
        String validJson = """
            {
                "productCode": "PROD-2026-99",
                "productName": "프리미엄 후드티",
                "categoryCode": "CT100",
                "productDescription": "최상급 쭈리 원단으로 제작된 후드티입니다."
            }
            """;

        // When
        ValidationStepResult<ProductStep> result = productProcessService.getProcessedProductReport(validJson);

        // Then
        assertTrue(result.isSuccess());                      // 1. 검증 결과 성공(true) 체크
        assertTrue(result.getErrorMessages().isEmpty());     // 2. 오류 리스트 비어있는지 체크
        assertNotNull(result.getPayload());                  // 3. 변환된 도메인 객체(toDomain) 존재 확인
        assertEquals("PROD-2026-99", result.getPayload().getProductCode());
    }

    @Test
    @DisplayName("실패 시나리오: 다중 오류가 발생해도 예외를 발생시키지 않고, 오류 내용 리스트와 실패 상태(false)를 리턴한다")
    void getProcessedProductReport_Fail_CollectAllErrors() {
        // Given: 상품코드(짧음), 상품명(짧음), 상세설명(용량초과) 다중 오류 상황
        String tooLongKoreanDescription = "한글 상세 설명이 500바이트를 훌륭히 초과하도록 유도하기 위해 매우 길게 길게 텍스트를 작성해 봅니다. ".repeat(10);
        String invalidJson = """
            {
                "productCode": "SHORT",
                "productName": "S",
                "categoryCode": "CT100",
                "productDescription": "%s"
            }
            """.formatted(tooLongKoreanDescription);

        // When: 예외가 터져서 비정상 종료가 되는지 확인 (assertDoesNotThrow 사용)
        ValidationStepResult<ProductStep> result = assertDoesNotThrow(() ->
                productProcessService.getProcessedProductReport(invalidJson)
        );

        // Then
        assertFalse(result.isSuccess());                     // 1. 검증 결과 실패(false) 리턴 확인
        assertNull(result.getPayload());                     // 2. 실패했으므로 도메인 객체는 결합되지 않고 null 보장

        // 3. 수집된 다중 오류 세부 리스트 체크
        System.out.println("🔥 수집된 리포트 오류 내용:");
        result.getErrorMessages().forEach(System.out::println);

        assertEquals(3, result.getErrorMessages().size()); // 세 가지 에러(코드, 상품명, 상세설명)가 모두 잡혔는지 확인
        assertTrue(result.getErrorMessages().stream().anyMatch(msg -> msg.contains("상품코드")));
        assertTrue(result.getErrorMessages().stream().anyMatch(msg -> msg.contains("상품명")));
        assertTrue(result.getErrorMessages().stream().anyMatch(msg -> msg.contains("상품상세설명")));
    }
}