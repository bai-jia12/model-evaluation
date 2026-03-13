package com.example.foldermanager.service;

import com.example.foldermanager.entity.FolderDetail;
import com.example.foldermanager.repository.FolderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class FolderDetailService {
    
    @Autowired
    private FolderDetailRepository folderDetailRepository;
    
    @Value("${folder.scan.path}")
    private String scanPath;
    
    @Transactional
    public int scanAndSaveFolders() {
        File baseDir = new File(scanPath);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new RuntimeException("文件夹路径不存在或不是目录: " + scanPath);
        }
        
        File[] folders = baseDir.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            return 0;
        }
        
        int count = 0;
        for (File folder : folders) {
            String folderName = folder.getName();
            FolderInfo info = parseFolderName(folderName);
            if (info != null) {
                Optional<FolderDetail> existing = folderDetailRepository.findByFolderSeq(info.seq);
                FolderDetail detail;
                if (existing.isPresent()) {
                    detail = existing.get();
                    detail.setName(info.name);
                    detail.setImageCount(info.imageCount);
                    detail.setFolderPath(folder.getAbsolutePath());
                } else {
                    detail = new FolderDetail();
                    detail.setFolderSeq(info.seq);
                    detail.setName(info.name);
                    detail.setImageCount(info.imageCount);
                    detail.setFolderPath(folder.getAbsolutePath());
                }
                folderDetailRepository.save(detail);
                count++;
            }
        }
        return count;
    }
    
    private FolderInfo parseFolderName(String folderName) {
        try {
            String[] parts = folderName.split("-");
            if (parts.length >= 3) {
                FolderInfo info = new FolderInfo();
                info.seq = Integer.parseInt(parts[0].trim());
                
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 1; i < parts.length - 1; i++) {
                    if (i > 1) nameBuilder.append("-");
                    nameBuilder.append(parts[i].trim());
                }
                info.name = nameBuilder.toString();
                
                String lastPart = parts[parts.length - 1].trim();
                info.imageCount = Integer.parseInt(lastPart);
                
                return info;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
    
    private static class FolderInfo {
        int seq;
        String name;
        int imageCount;
    }
    
    public List<FolderDetail> getAllFolders() {
        return folderDetailRepository.findAllOrderByFolderSeqAsc();
    }
}
