package com.practise.userservice.exceptionHandling.exceptions;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String string) {
        super(string);
    }
}
