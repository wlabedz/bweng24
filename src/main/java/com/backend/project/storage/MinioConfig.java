package com.backend.project.storage;
import com.backend.project.property.MinioProperties;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    private final MinioProperties minioProperties;

    public MinioConfig(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .credentials(
                        minioProperties.getUser(),
                        minioProperties.getPassword()
                )
                .endpoint(minioProperties.getUrl(),
                        minioProperties.getPort(),
                        minioProperties.getUrl().contains("https"))
                .build();
    }
}
