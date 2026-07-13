package com.example.validator;

import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class LengthValidator {
    private final List<Runnable> checks = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    public LengthValidator add(String label, String value, int min, int max) {
        checks.add(() -> {
            if (value == null) return;
            int len = value.length();
            if (len < min || len > max) {
                errorMessages.add(String.format("[%s]은(는) %d~%d자 사이여야 합니다. (입력: %d자)", label, min, max, len));
            }
        });
        return this;
    }

    /**
     * 추가: 바이트 수(Byte) 기반 최대치 검증 (UTF-8 기준)
     */
    public LengthValidator addByteCheck(String label, String value, int maxByte) {
        checks.add(() -> {
            if (value == null) return;

            // UTF-8 기준으로 문자열을 바이트 배열로 변환하여 길이 측정
            int byteLength = value.getBytes(StandardCharsets.UTF_8).length;

            if (byteLength > maxByte) {
                errorMessages.add(String.format("[%s]의 허용 용량은 %d byte입니다. (현재 입력: %d byte)", label, maxByte, byteLength));
            }
        });
        return this;
    }

    public void execute() {
        for (Runnable check : checks) {
            check.run();
        }
        if (!errorMessages.isEmpty()) {
            String combinedMessage = errorMessages.stream().collect(Collectors.joining(", "));
            throw new IllegalArgumentException(combinedMessage);
        }
    }
}