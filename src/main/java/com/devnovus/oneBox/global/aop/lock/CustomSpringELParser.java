package com.devnovus.oneBox.global.aop.lock;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * SpEL(Spring Expression Language)을 이용해
 * AOP에서 메서드의 파라미터 값을 동적으로 추출하기 위한 유틸리티 클래스
 */
public class CustomSpringELParser {
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    // 유틸리티 클래스이므로 인스턴스화 방지
    private CustomSpringELParser() {}

    // SpEL 표현식(key)을 실제 메서드 인자 값으로 평가하여 반환
    public static Long getDynamicValue(String[] parameterNames, Object[] args, String key) {
        // SpEL이 평가될 컨텍스트(변수 환경) 생성
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 메서드의 파라미터 이름과 실제 값을 SpEL 컨텍스트 변수로 등록
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        // 사용자가 입력한 key를 바탕으로 값을 파싱
        Object value = PARSER.parseExpression(key).getValue(context);

        if (value == null) {
            throw new IllegalStateException("SpEL key가 비어있습니다: " + key);
        }

        return (Long) value;
    }
}
