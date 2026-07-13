package com.example.validator;

import lombok.NoArgsConstructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class LengthValidator {
    private final List<Runnable> checks = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    // 💡 [추가] 등록된 필드들의 변수명(Key)과 설명(Label)을 저장하는 맵
    private final Map<String, String> registeredFields = new HashMap<>();

    /**
     * 기존 검증 함수 확장 (필드 변수명인 fieldKey를 추가로 받습니다)
     */
    public LengthValidator add(String fieldKey, String label, String value, int min, int max) {
        registeredFields.put(fieldKey, label); // 구조 비교용 데이터 수집

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
     * 바이트 체크 함수 확장
     */
    public LengthValidator addByteCheck(String fieldKey, String label, String value, int maxByte) {
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

    // 💡 [추가] 현재 빌더에 등록된 필드 목록(영어 변수명)을 반환하는 메서드
    public Set<String> getTargetFields() {
        return registeredFields.keySet();
    }

    // 💡 [추가] 변수명에 해당하는 한글 라벨을 반환하는 메서드 (에러 메시지용)
    public String getLabel(String fieldKey) {
        return registeredFields.getOrDefault(fieldKey, fieldKey);
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