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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    private final RedissonClient redissonClient;
    private final MetadataRepository metadataRepository;
    private final TransactionalExecutor transactionalExecutor;

    @Around("@annotation(DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        // 메서드 및 어노테이션 정보 획득
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // key 생성
        Long folderId = CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                distributedLock.key()
        );
        Long ownerId = metadataRepository.findOwnerIdById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
        String key = "LOCK:" + ownerId;

        // RLock 객체 획득
        RLock rLock = redissonClient.getLock(key);

        // Lock 획득 시도
        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.timeUnit());

            if (!available) {
                log.info("[Redisson] FAIL TO ACQUIRE LOCK: {} {}", method.getName(), key);
                return false;
            }

            log.info("[Redisson] SUCCESS TO ACQUIRE LOCK: {} {}", method.getName(), key);
            return transactionalExecutor.proceed(joinPoint);

        } catch (InterruptedException e) {
            throw new InterruptedException();

        } finally {

            // 트랜잭션 종료 시 Lock 해제
            try {
                rLock.unlock();
                log.info("[Redisson] SUCCESS TO DO UNLOCK: {} {}", method.getName(), key);

            } catch (IllegalMonitorStateException e) {
                log.info("[Redisson] ALREADY UNLOCKED: {} {}", method.getName(), key);
            }
        }
    }
}
