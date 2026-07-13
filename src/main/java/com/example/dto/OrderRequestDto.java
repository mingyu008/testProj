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
     * DTO 내부 필드의 정합성을 스스로 체크하는 함수
     */
    public void validate() {
        new LengthValidator()
                .add("주문번호", this.orderId, 10, 20)
                .add("상품명", this.productName, 1, 100)
//                .add("수령인", this.receiverName, 2, 10)
                .addByteCheck("수령인", this.receiverName, 30)
                .add("전화번호", this.phoneNumber, 10, 11)
                .add("배송메모", this.deliveryMemo, 0, 200)
                .execute();
    }
}