package com.backend.project.exceptions;

public class OfficeNotFoundException extends RuntimeException {
    public OfficeNotFoundException(String id) {
        super("Office not found with id: " + id);
    }
}