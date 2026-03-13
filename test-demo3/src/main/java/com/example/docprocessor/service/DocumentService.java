package com.example.docprocessor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    public String extractText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return "";
        }
        fileName = fileName.toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return extractTextFromPdf(file.getInputStream());
        } else if (fileName.endsWith(".docx")) {
            return extractTextFromDocx(file.getInputStream());
        } else if (fileName.endsWith(".doc")) {
            return extractTextFromDoc(file.getInputStream());
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } else {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractTextFromDoc(InputStream inputStream) throws IOException {
        try (WordExtractor extractor = new WordExtractor(inputStream)) {
            return extractor.getText();
        }
    }

    public List<String> splitText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        text = text.replaceAll("\\s+", " ");
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

    public List<String> splitTextBy5000Chars(String text) {
        return splitText(text, 5000);
    }
}
