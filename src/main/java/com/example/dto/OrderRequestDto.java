package com.example.dto;

import com.example.validator.LengthValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    private String orderId;
    private String productName;
    private String receiverName;
    private String phoneNumber;
    private String deliveryMemo;

    /**
     * 💡 핵심: 구조만 먼저 추출하거나 검증을 수행할 공통 빌더 인스턴스 생성 매핑
     */
    public LengthValidator getValidatorSpecification() {
        return new LengthValidator()
                // .add("필드변수명", "한글라벨", 값, 최소, 최대)
                .add("orderId", "주문번호", this.orderId, 10, 20)
                .add("productName", "상품명", this.productName, 1, 100)
                .add("receiverName", "수령인", this.receiverName, 2, 10)
                .addByteCheck("deliveryMemo", "배송메모", this.deliveryMemo, 200);
        // phoneNumber는 비필수 타겟이라 치고 제외하면 자동으로 구조 대조에서도 빠집니다!
    }

    /**
     * 기존에 서비스가 호출하던 검증 메서드
     */
    public void validate() {
        getValidatorSpecification().execute();
    }
}