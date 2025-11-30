package com.devnovus.oneBox.global.aop.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalExecutor {
    // 락 해제 타이밍을 트랜잭션과 완전히 분리해 올바른 순서로 보장하기 위해 REQUIRES_NEW 사용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
