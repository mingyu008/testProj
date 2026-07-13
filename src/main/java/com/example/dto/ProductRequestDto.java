package com.example.dto;

import com.example.validator.LengthValidator;
import com.example.validator.ValidatableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto implements ValidatableDto {
    private String productCode;
    private String productName;
    private String categoryCode;
    private String manufacturer;
    private String productDescription;

    @Override
    public LengthValidator getValidatorSpecification() {
        return new LengthValidator()
                .add("productCode", "상품코드", this.productCode, 8, 12)
                .add("productName", "상품명", this.productName, 2, 50)
                .add("categoryCode", "카테고리코드", this.categoryCode, 3, 5)
                // 상세 설명은 영문/한글 혼용 시 안전하도록 DB 컬럼 기준 최대 500바이트 검증
                .addByteCheck("productDescription", "상품상세설명", this.productDescription, 500);
        // manufacturer(제조사)는 누락되어도 통과하는 선택 입력값이라 치고 명세에서 제외!
    }
}