package com.backend.project.exceptions;


public class FailedUploadingPhoto extends Exception {
    public FailedUploadingPhoto(String message) {
        super(message);
    }
}