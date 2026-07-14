package com.step.dto;

import com.step.validator.LengthValidatorStep;
import com.step.validator.ValidatableStepDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestStepDto implements ValidatableStepDto {
    private String productCode;
    private String productName;
    private String categoryCode;
    private String manufacturer;
    private String productDescription;

    @Override
    public LengthValidatorStep getValidatorSpecification() {
        return new LengthValidatorStep()
                .add("productCode", "상품코드", this.productCode, 8, 12)
                .add("productName", "상품명", this.productName, 2, 50)
                .add("categoryCode", "카테고리코드", this.categoryCode, 3, 5)
                // 상세 설명은 영문/한글 혼용 시 안전하도록 DB 컬럼 기준 최대 500바이트 검증
                .addByteCheck("productDescription", "상품상세설명", this.productDescription, 500);
        // manufacturer(제조사)는 누락되어도 통과하는 선택 입력값이라 치고 명세에서 제외!
    }
}