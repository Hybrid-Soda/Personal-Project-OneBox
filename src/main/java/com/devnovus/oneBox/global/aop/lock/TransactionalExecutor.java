package com.devnovus.oneBox.global.aop.lock;

import com.devnovus.oneBox.domain.metadata.repository.AdvisoryLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionalExecutor {
    private final AdvisoryLockRepository advisoryLockRepository;

    // 락 해제 타이밍을 트랜잭션과 완전히 분리해 올바른 순서로 보장하기 위해 REQUIRES_NEW 사용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint, final Long ownerId) throws Throwable {
        log.info("[Lock] try lock: {}", ownerId);
        advisoryLockRepository.acquireTxLock(ownerId);
        return joinPoint.proceed();
    }
}
