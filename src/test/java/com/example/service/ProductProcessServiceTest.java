package com.example.service;

import com.example.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 스프링 컨텍스트를 띄워 제네릭 서비스와 매퍼 빈을 주입받습니다.
class ProductProcessServiceTest {

    @Autowired
    private ProductProcessService productProcessService;

    @Test
    @DisplayName("성공 케이스: 모든 길이 규칙과 선별 필수 구조를 만족하는 JSON이 들어오면, 도메인 객체(Product)로 안전하게 변환된다")
    void processIncomingProductJson_Success() {
        // Given: 상품코드(11자), 상품명(12자), 카테고리(4자), 상세설명(정상)이 모두 포함된 JSON
        // 명세에서 제외했던 'manufacturer(제조사)'는 넣지 않아도 구조 검증을 통과합니다.
        String validJson = """
            {
                "productCode": "PROD-2026-99",
                "productName": "프리미엄 무지 맨투맨",
                "categoryCode": "CT01",
                "productDescription": "최고급 면 원사를 사용하여 사계절 내내 착용하기 좋은 오버핏 맨투맨입니다."
            }
            """;

        // When: 제네릭 구조 대조 -> 파싱 -> 값 검증 -> MapStruct 도메인 매핑 실행
        Product result = productProcessService.processIncomingProductJson(validJson);

        // Then: 매핑 결과 및 데이터 정합성 검증
        assertNotNull(result);
        assertEquals("PROD-2026-99", result.getProductCode());
        assertEquals("프리미엄 무지 맨투맨", result.getProductName());
        assertEquals("CT01", result.getCategoryCode());
        assertNull(result.getManufacturer()); // JSON에 없었으므로 null로 안전하게 매핑
        System.out.println("성공");
    }

    @Test
    @DisplayName("실패 케이스: 롬복/어노테이션 없이 빌더 명세에 적어둔 필수 필드(categoryCode)가 누락되면 한글 라벨명과 함께 에러를 던진다")
    void processIncomingProductJson_Fail_StructureMissing() {
        // Given: 카테고리코드('categoryCode')가 통째로 빠진 불완전한 JSON 데이터
        String invalidJson = """
            {
                "productCode": "PROD-2026-99",
                "productName": "프리미엄 무지 맨투맨",
                "productDescription": "상세 설명 텍스트"
            }
            """;

        // When & Then: 파싱이 시작되기도 전에 입구 컷을 당해 IllegalArgumentException이 터지는지 확인
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productProcessService.processIncomingProductJson(invalidJson)
        );

        String message = exception.getMessage();
        System.out.println("🔥 발생한 제네릭 구조 예외 메시지:");
        System.out.println(message);

        // 영어 필드명이 아닌, 빌더에 선언해 둔 한글 라벨 '카테고리코드'가 메시지에 찍혔는지 확인
        assertTrue(message.contains("필수 입력 항목이 누락되었습니다"));
        assertTrue(message.contains("카테고리코드"));
    }

    @Test
    @DisplayName("실패 케이스: 구조는 맞지만 상품코드의 글자 수가 제약 조건(8~12자)을 벗어나 짧으면 값 검증 단계에서 예외가 발생한다")
    void processIncomingProductJson_Fail_ValueLengthInvalid() {
        // Given: 'productCode'가 4자로 기준치(최소 8자)보다 너무 짧은 경우
        String invalidLengthJson = """
            {
                "productCode": "SHORT",
                "productName": "슬림핏 라운드 티셔츠",
                "categoryCode": "CT02",
                "productDescription": "상세 설명 내용"
            }
            """;

        // When & Then: 구조 검증은 넘어가지만 6단계인 fullyPopulatedDto.validate()에서 포착됨
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productProcessService.processIncomingProductJson(invalidLengthJson)
        );

        String message = exception.getMessage();
        System.out.println("🔥 발생한 세부 값 검증 예외 메시지: " + message);

        assertTrue(message.contains("상품코드"));
        assertTrue(message.contains("8~12자 사이여야 합니다"));
    }
}