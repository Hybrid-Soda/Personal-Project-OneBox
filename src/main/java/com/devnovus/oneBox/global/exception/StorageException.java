package com.devnovus.oneBox.global.exception;

public class StorageException extends RuntimeException {

    public StorageException(Exception e) {
        super(e.getMessage());
    }
}
