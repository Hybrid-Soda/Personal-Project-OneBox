package com.devnovus.oneBox.global.aop.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdvisoryLock {
    String metadataId();
}
