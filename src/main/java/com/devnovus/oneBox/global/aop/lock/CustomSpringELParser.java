package com.devnovus.oneBox.global.aop.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CustomSpringELParser {
    private CustomSpringELParser() {}

    public static Long getDynamicValue(final ProceedingJoinPoint joinPoint) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();

        String key = signature.getMethod().getAnnotation(AdvisoryLock.class).metadataId();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return (Long) parser.parseExpression(key).getValue(context);
    }
}
