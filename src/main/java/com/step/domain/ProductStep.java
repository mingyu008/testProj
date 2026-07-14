package com.step.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStep {
    private String productCode;
    private String productName;
    private String categoryCode;
    private String manufacturer;
    private String productDescription;
}