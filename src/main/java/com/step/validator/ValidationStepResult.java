package com.step.validator;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationStepResult<T> {
    private final boolean success;              // 검증 성공 여부 (상태)
    private final List<String> errorMessages;   // 구체적인 오류 내용 리스트
    private final T payload;                    // 성공 시 반환할 도메인 객체 (toDomain 결과)

    // 내부 생성자 (정적 팩토리 메서드를 사용하도록 제한)
    private ValidationStepResult(boolean success, List<String> errorMessages, T payload) {
        this.success = success;
        this.errorMessages = errorMessages != null ? errorMessages : new ArrayList<>();
        this.payload = payload;
    }

    /**
     * 성공 시 결과를 담는 팩토리 메서드
     */
    public static <T> ValidationStepResult<T> success(T payload) {
        return new ValidationStepResult<>(true, null, payload);
    }

    /**
     * 실패 시 결과를 담는 팩토리 메서드
     */
    public static <T> ValidationStepResult<T> fail(List<String> errorMessages) {
        return new ValidationStepResult<>(false, errorMessages, null);
    }
}