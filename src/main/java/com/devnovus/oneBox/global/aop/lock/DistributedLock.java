package com.devnovus.oneBox.global.aop.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    // 락 이름
    String key();

    // 락 시간단위 (default = ms)
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    // 락 대기시간 (default = 5s)
    long waitTime() default 5000L;
}
