package com.example.service;

import com.example.domain.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 스프링 빈(Service, Mapper)을 주입받기 위해 선언합니다.
class OrderProcessServiceTest {

    @Autowired
    private OrderProcessService orderProcessService;

    @Test
    @DisplayName("성공 케이스: 모든 길이 규칙을 만족하는 JSON이 들어오면, 파싱 및 검증을 거쳐 도메인 객체(Order)로 완벽히 매핑된다")
    void processIncomingOrderJson_Success() {
        // Given: 모든 제약조건을 만족하는 완벽한 2026년형 JSON 데이터
        String validJson = """
            {
                "orderId": "ORD-2026-0713",
                "productName": "고급 오버핏 티셔츠 XXL",
                "receiverName": "김철수",
                "phoneNumber": "01012345678",
                "deliveryMemo": "문 앞에 두고 벨 눌러주세요."
            }
            """;

        // When: 파싱 -> 검증 -> MapStruct 매핑 과정을 한 번에 수행
        Order result = orderProcessService.processIncomingOrderJson(validJson);

        // Then: 매핑된 도메인 객체의 데이터 검증 (Lombok @Getter 기반)
        assertNotNull(result);
        assertEquals("ORD-2026-0713", result.getOrderId());
        assertEquals("고급 오버핏 티셔츠 XXL", result.getProductName());
        assertEquals("김철수", result.getReceiverName());
        assertEquals("01012345678", result.getPhoneNumber());
        assertEquals("문 앞에 두고 벨 눌러주세요.", result.getDeliveryMemo());
    }

    @Test
    @DisplayName("실패 케이스: 여러 필드가 한 번에 글자 수 제한을 위반하면, 누락 없이 모든 에러 메시지를 수집하여 예외를 발생시킨다")
    void processIncomingOrderJson_Fail_ValidationError() {
        // Given: 주문번호(짧음), 수령인(짧음), 전화번호(짧음)가 동시에 규칙을 위반한 JSON
        String invalidJson = """
            {
                "orderId": "SHORT",
                "productName": "자바/스프링 백엔드 교과서",
                "receiverName": "최",
                "phoneNumber": "0100",
                "deliveryMemo": "안전 배송 바랍니다."
            }
            """;

        // When & Then: 수집된 복합 예외 메시지 검증
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderProcessService.processIncomingOrderJson(invalidJson)
        );

        String message = exception.getMessage();
        System.out.println("🔥 발생한 복합 에러 메시지 내용:");
        System.out.println(message);

        // 빌더가 누락 없이 모든 위반 필드를 찾아냈는지 확인
        assertTrue(message.contains("주문번호"));
        assertTrue(message.contains("수령인"));
        assertTrue(message.contains("전화번호"));
    }

    @Test
    @DisplayName("실패 케이스: JSON 문법 자체가 파괴되어 들어오면 Jackson 파싱 단계에서 거절된다")
    void processIncomingOrderJson_Fail_InvalidJsonSyntax() {
        // Given: 콤마(,)가 빠지거나 중괄호가 안 닫힌 잘못된 문법의 JSON
        String brokenJson = """
            {
                "orderId": "ORD-2026-0713"
                "productName": "문법이 깨진 상품"
            }
            """; // orderId 뒤에 콤마(,) 누락

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderProcessService.processIncomingOrderJson(brokenJson)
        );

        System.out.println("🔥 발생한 문법 에러 메시지 내용: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("올바르지 않은 JSON 형식입니다."));
    }

    @Test
    @DisplayName("실패 케이스: 배송메모 필드가 영문/숫자 기준은 충족하지만 한글 바이트 용량(200byte)을 초과하면 예외가 발생한다")
    void processIncomingOrderJson_Fail_ByteOver() {
        // Given: 70자 가량의 한글 텍스트 (UTF-8 한글은 자당 3바이트이므로 70자 * 3 = 210바이트로 200바이트를 초과)
        String longKoreanMemo = "한글은한글자당삼바이트를차지하기때문에글자수제한을아무리넉넉하게준다고하더라도디비바이트용량에걸려";

        String invalidJson = """
        {
            "orderId": "ORD-2026-0713",
            "productName": "자바/스프링 백엔드 교과서",
            "receiverName": "홍길동홍길동홍길동홍길동",
            "phoneNumber": "01012345678",
            "deliveryMemo": "%s"
        }
        """.formatted(longKoreanMemo);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderProcessService.processIncomingOrderJson(invalidJson)
        );

        String message = exception.getMessage();
        System.out.println("🔥 발생한 바이트 에러 메시지 내용:");
        System.out.println(message);

        assertTrue(message.contains("배송메모"));
        assertTrue(message.contains("허용 용량은 200 byte입니다"));
    }

    @Test
    @DisplayName("실패 케이스: 빌더에 add 된 필수 필드(productName)가 누락되면 명확한 한글 라벨명과 함께 구조 오류를 던진다")
    void processIncomingOrderJson_Fail_LabelSpecificationMissing() {
        // Given: 빌더에는 명시되어 있지만 JSON에는 'productName'이 누락됨
        String invalidJson = """
        {
            "orderId": "ORD-2026-0713",
            "receiverName": "홍길동",
            "deliveryMemo": "안전 배송 바랍니다."
        }
        """;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderProcessService.processIncomingOrderJson(invalidJson)
        );

        String message = exception.getMessage();
        System.out.println("🔥 출력된 구조 에러 메시지: " + message);

        // 'productName' 대신 빌더에 매핑해 둔 한글 라벨 '상품명'이 메시지에 박히는지 검증합니다.
        assertTrue(message.contains("필수 입력 항목이 JSON 데이터에서 누락되었습니다"));
        assertTrue(message.contains("상품명"));
    }
}