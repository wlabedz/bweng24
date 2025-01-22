package com.backend.project.exceptions;

public class UsernameForbiddenException extends Exception {
    public UsernameForbiddenException(String email) {
        super("Username " + email + " is forbidden");
    }
}