package com.backend.project.exceptions;

public class NotAllowedException extends Exception {
    public NotAllowedException(String username) {
        super(username + " is not allowed to access this resource");
    }
}