package com.example.docprocessor.controller;

import com.example.docprocessor.dto.DocumentResult;
import com.example.docprocessor.service.DocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFiles(@RequestParam(value = "files", required = false) MultipartFile[] files,
                              @RequestParam(value = "prompt", required = false, defaultValue = "") String prompt,
                              Model model) {
        List<DocumentResult> results = new ArrayList<>();
        
        if (files == null || files.length == 0) {
            model.addAttribute("error", "请至少选择一个文件");
            return "index";
        }

        if (files.length > 10) {
            model.addAttribute("error", "最多只能上传10个文件");
            return "index";
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            try {
                String text = documentService.extractText(file);
                List<String> chunks = documentService.splitTextBy5000Chars(text);
                results.add(new DocumentResult(file.getOriginalFilename(), chunks, prompt));
            } catch (IOException e) {
                model.addAttribute("error", "处理文件时出错: " + file.getOriginalFilename());
                return "index";
            }
        }

        model.addAttribute("results", results);
        return "result";
    }
}
