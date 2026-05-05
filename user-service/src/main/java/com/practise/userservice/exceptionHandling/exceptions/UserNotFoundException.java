package com.practise.userservice.exceptionHandling.exceptions;

import org.springframework.web.bind.annotation.ExceptionHandler;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String string) {
        super(string);
    }
}
