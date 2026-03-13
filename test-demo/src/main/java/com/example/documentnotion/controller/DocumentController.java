package com.example.documentnotion.controller;

import com.example.documentnotion.dto.ProcessResult;
import com.example.documentnotion.dto.UploadRequest;
import com.example.documentnotion.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/api/documents/upload")
    @ResponseBody
    public ResponseEntity<?> uploadDocuments(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("prompt") String prompt) {
        
        log.info("接收到 {} 个文件上传请求", files.size());
        
        if (files.isEmpty()) {
            return ResponseEntity.badRequest().body("请至少上传一个文件");
        }
        
        if (files.size() > 10) {
            return ResponseEntity.badRequest().body("最多只能上传10个文件");
        }
        
        List<ProcessResult> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                ProcessResult result = documentProcessingService.processDocument(file, prompt);
                results.add(result);
            } catch (Exception e) {
                log.error("处理文件 {} 时出错: {}", file.getOriginalFilename(), e.getMessage());
                results.add(ProcessResult.builder()
                        .fileName(file.getOriginalFilename())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }
        
        return ResponseEntity.ok(results);
    }
}
