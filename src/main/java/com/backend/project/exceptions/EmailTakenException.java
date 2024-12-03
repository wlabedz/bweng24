package com.backend.project.exceptions;

public class EmailTakenException extends Exception {
    public EmailTakenException(String email) {
        super("Email " + email + " is already taken");
    }
}