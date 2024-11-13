package com.backend.project.exceptions;

import java.util.UUID;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(UUID id) {
        super("Item not found with id: " + id);
    }
}