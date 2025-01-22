package com.backend.project.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("minio")
public class MinioProperties {
    private String url;
    private int port;
    private String user;
    private String password;
    private String bucket;
}
