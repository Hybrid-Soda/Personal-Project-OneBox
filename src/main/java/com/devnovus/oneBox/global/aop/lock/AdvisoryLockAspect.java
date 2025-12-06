package com.devnovus.oneBox.global.aop.lock;

import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class AdvisoryLockAspect {
    private final MetadataRepository metadataRepository;
    private final TransactionalExecutor transactionalExecutor;

    @Around("@annotation(AdvisoryLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        Long folderId = CustomSpringELParser.getDynamicValue(joinPoint);
        Long ownerId = metadataRepository.findOwnerIdById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        return transactionalExecutor.proceed(joinPoint, ownerId);
    }
}
