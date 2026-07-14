package com.step.validator;

import lombok.NoArgsConstructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
public class LengthValidatorStep {
    private final List<Runnable> checks = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    private final Map<String, String> registeredFields = new HashMap<>();

    public LengthValidatorStep add(String fieldKey, String label, String value, int min, int max) {
        registeredFields.put(fieldKey, label);
        checks.add(() -> {
            if (value == null) return;
            int len = value.length();
            if (len < min || len > max) {
                errorMessages.add(String.format("[%s]은(는) %d~%d자 사이여야 합니다. (입력: %d자)", label, min, max, len));
            }
        });
        return this;
    }

    public LengthValidatorStep addByteCheck(String fieldKey, String label, String value, int maxByte) {
        registeredFields.put(fieldKey, label);
        checks.add(() -> {
            if (value == null) return;
            int byteLength = value.getBytes(StandardCharsets.UTF_8).length;
            if (byteLength > maxByte) {
                errorMessages.add(String.format("[%s]의 허용 용량은 %d byte입니다. (현재 입력: %d byte)", label, maxByte, byteLength));
            }
        });
        return this;
    }

    public Set<String> getTargetFields() {
        return registeredFields.keySet();
    }

    public String getLabel(String fieldKey) {
        return registeredFields.getOrDefault(fieldKey, fieldKey);
    }

    // 💡 [변경] 예외를 던지는 대신, 검증을 전체 수행하고 발견된 오류 메시지들을 반환
    public List<String> getErrorMessages() {
        for (Runnable check : checks) {
            check.run();
        }
        return errorMessages;
    }
}
