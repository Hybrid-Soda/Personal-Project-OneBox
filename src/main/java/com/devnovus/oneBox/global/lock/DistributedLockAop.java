package com.devnovus.oneBox.global.lock;

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
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 어노테이션 정보 획득
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // 스프링 SPEL 혹은 사용자 지정 파서로 동적 키 생성
        String key = "LOCK:" + CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                distributedLock.key()
        );

        // Redisson을 통해 RLock 객체 획득
        RLock rLock = redissonClient.getLock(key);

        try {
            log.info(String.valueOf(distributedLock));
            // 락 획득 시도: waitTime 동안 대기하고, leaseTime 만큼 락을 보유
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.timeUnit());

            // // 락 획득 실패 시 false 리턴
            if (!available) {
                log.info("FAIL TO ACQUIRE LOCK: {} {}", method.getName(), key);
                return false;
            }

            // 락을 획득한 상태에서 실제 메소드 실행
            log.info("SUCCESS TO ACQUIRE LOCK: {} {}", method.getName(), key);
            return aopForTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            // 스레드 인터럽트 시 예외 재던짐
            throw new InterruptedException();
        } finally {
            try {
                rLock.unlock();
                log.info("SUCCESS TO DO UNLOCK: {} {}", method.getName(), key);
            } catch (IllegalMonitorStateException e) {
                // 현재 스레드가 락 소유자가 아니거나 이미 해제된 상태에서 unlock 호출 시 발생 가능
                log.info("FAIL TO DO UNLOCK: {} {}", method.getName(), key);
            }
        }
    }
}
