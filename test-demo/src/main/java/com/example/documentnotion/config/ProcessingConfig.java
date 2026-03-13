package com.example.documentnotion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "processing")
public class ProcessingConfig {
    private int chunkSize = 5000;
    private String uploadDir;
}
