package com.example.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) // 필드에 붙이는 어노테이션
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지되어 검증기가 읽을 수 있게 함
public @interface CheckTarget {
    String value() default ""; // 에러 메시지에 노출할 필드 설명 (선택)
}