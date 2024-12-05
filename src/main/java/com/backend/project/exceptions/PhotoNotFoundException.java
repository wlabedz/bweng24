package com.backend.project.exceptions;

import java.util.UUID;

public class PhotoNotFoundException extends RuntimeException {
    public PhotoNotFoundException(UUID id) {
        super("Photo not found with id: " + id);
    }
}