package com.backend.project.storage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Component
public interface FileStorage {
    String upload(MultipartFile file);
    InputStream download(String id);
    void deleteFile(String id);
}
