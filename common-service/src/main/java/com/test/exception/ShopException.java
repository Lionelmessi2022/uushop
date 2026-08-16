package com.test.exception;

import com.test.result.ResponseEnum;

public class ShopException extends RuntimeException {
    public ShopException(String message) {
        super(message);
    }


}