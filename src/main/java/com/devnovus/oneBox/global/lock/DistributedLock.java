package com.devnovus.oneBox.global.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    // 락 이름
    String key();

    // 락 시간단위
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    // 락 대기시간 (default = 5s)
    long waitTime() default 5000L;
}
