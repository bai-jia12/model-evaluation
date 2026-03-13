package com.example.documentnotion.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UploadRequest {
    private List<MultipartFile> files;
    private String prompt;
}
