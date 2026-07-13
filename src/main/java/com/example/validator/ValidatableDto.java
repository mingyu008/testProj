package com.example.validator;

// (공통 인터페이스)
public interface ValidatableDto {
    LengthValidator getValidatorSpecification();

    default void validate() {
        LengthValidator spec = getValidatorSpecification();
        if (spec != null) {
            spec.execute();
        }
    }
}