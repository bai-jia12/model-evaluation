package com.example.docproc.controller;

import com.example.docproc.dto.DocumentResult;
import com.example.docproc.service.DocumentParserService;
import com.example.docproc.service.TextSplitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
public class DocumentController {

    @Autowired
    private DocumentParserService documentParserService;

    @Autowired
    private TextSplitService textSplitService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadDocuments(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "prompt", required = false) String prompt) {
        
        Map<String, Object> response = new HashMap<>();
        List<DocumentResult> results = new ArrayList<>();

        if (files == null || files.length == 0) {
            response.put("success", false);
            response.put("message", "请选择至少一个文件");
            return response;
        }

        if (files.length > 10) {
            response.put("success", false);
            response.put("message", "最多只能上传10个文件");
            return response;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            DocumentResult result = new DocumentResult();
            result.setFileName(file.getOriginalFilename());
            result.setFileSize(formatFileSize(file.getSize()));

            try {
                String text = documentParserService.parseDocument(file);
                result.setOriginalText(text);
                result.setCharCount(text.length());

                List<String> chunks = textSplitService.splitText(text);
                result.setChunks(chunks);
                result.setChunkCount(chunks.size());

                String mergedContent = textSplitService.mergeChunks(chunks, prompt);
                result.setMergedContent(mergedContent);
            } catch (Exception e) {
                result.setError("处理失败: " + e.getMessage());
            }
            results.add(result);
        }

        response.put("success", true);
        response.put("results", results);
        return response;
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        return String.format("%.2f MB", size / (1024.0 * 1024));
    }
}
