package com.step.service;

import com.step.validator.LengthValidatorStep;
import com.step.validator.ValidatableStepDto;
import com.step.validator.ValidationStepResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
public class CommonJsonProcessStepService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON을 안전하게 검증하여 예외 없이 검증 상태, 오류 세부내역, 도메인 결과까지 한꺼번에 반환하는 서비스
     *
     * @param jsonString 입력받은 JSON 데이터
     * @param clazz 대상 DTO 클래스 타입
     * @param domainMapper DTO를 도메인 객체로 변환해 줄 매퍼 함수 (람다)
     * @param <T> DTO 타입
     * @param <R> 매핑될 최종 도메인 객체 타입
     * @return 검증 상태와 결과물이 묶인 ValidationResult 객체
     */
    public <T extends ValidatableStepDto, R> ValidationStepResult<R> parseAndValidateSafely(
            String jsonString, Class<T> clazz, Function<T, R> domainMapper) {

        List<String> accumulatedErrors = new ArrayList<>();

        try {
            // 1. DTO 구조적 필드 명세 확보
            T emptyDtoInstance = clazz.getDeclaredConstructor().newInstance();
            LengthValidatorStep spec = emptyDtoInstance.getValidatorSpecification();

            if (spec != null) {
                Set<String> requiredTargetFields = spec.getTargetFields();

                // 2. 수신된 JSON의 Key 파싱
                JsonNode rootNode = objectMapper.readTree(jsonString);
                List<String> jsonKeys = new ArrayList<>();
                Iterator<String> fieldNames = rootNode.fieldNames();
                while (fieldNames.hasNext()) {
                    jsonKeys.add(fieldNames.next());
                }

                // 3. 필수 구조 누락 대조 및 수집
                for (String requiredField : requiredTargetFields) {
                    if (!jsonKeys.contains(requiredField)) {
                        accumulatedErrors.add(String.format("필수 입력 항목이 누락되었습니다: [%s]", spec.getLabel(requiredField)));
                    }
                }
            }

            // 필수 구조에 이미 오류가 있는 경우, 더 이상 세부 값 체크를 하지 않고 실패 결과를 반환
            if (!accumulatedErrors.isEmpty()) {
                return ValidationStepResult.fail(accumulatedErrors);
            }

            // 4. 구조 통과 시 실제 데이터 바인딩 파싱
            T fullyPopulatedDto = objectMapper.readValue(jsonString, clazz);

            // 5. 세부 값 검증(길이, 바이트) 결과 수집
            LengthValidatorStep valueSpec = fullyPopulatedDto.getValidatorSpecification();
            if (valueSpec != null) {
                accumulatedErrors.addAll(valueSpec.getErrorMessages());
            }

            // 세부 값 검증 단계에서 실패한 경우
            if (!accumulatedErrors.isEmpty()) {
                return ValidationStepResult.fail(accumulatedErrors);
            }

            // 6. 모든 검증에 통과한 경우 MapStruct 매퍼를 통해 도메인 엔티티(R)로 매핑 후 반환
            R domainObj = domainMapper.apply(fullyPopulatedDto);
            return ValidationStepResult.success(domainObj);

        } catch (Exception e) {
            // JSON 문법 파괴 등 복구가 불가능한 시스템 수준 오류는 실패 묶음으로 전환
            accumulatedErrors.add("데이터 변환 및 파싱 처리에 실패했습니다. 이유: " + e.getMessage());
            return ValidationStepResult.fail(accumulatedErrors);
        }
    }
}