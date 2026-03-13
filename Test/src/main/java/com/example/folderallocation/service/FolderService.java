package com.example.folderallocation.service;

import com.example.folderallocation.entity.FolderDetail;
import com.example.folderallocation.repository.FolderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FolderService {

    @Autowired
    private FolderDetailRepository folderDetailRepository;

    public List<FolderDetail> scanAndSaveFolders() {
        String basePath = "d:\\datapicture";
        File baseDir = new File(basePath);
        List<FolderDetail> result = new ArrayList<>();

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return result;
        }

        File[] folders = baseDir.listFiles(File::isDirectory);
        if (folders == null) {
            return result;
        }

        for (File folder : folders) {
            String folderName = folder.getName();
            String[] parts = folderName.split("-");
            if (parts.length >= 3) {
                try {
                    int seq = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    int picCount = Integer.parseInt(parts[2]);

                    Optional<FolderDetail> existing = folderDetailRepository.findByFolderSeq(seq);
                    FolderDetail folderDetail;

                    if (existing.isPresent()) {
                        folderDetail = existing.get();
                    } else {
                        folderDetail = new FolderDetail();
                    }

                    folderDetail.setFolderSeq(seq);
                    folderDetail.setName(name);
                    folderDetail.setPicCount(picCount);
                    folderDetail.setFolderPath(folder.getAbsolutePath());

                    result.add(folderDetailRepository.save(folderDetail));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        return result;
    }

    public List<FolderDetail> getAllFolders() {
        return folderDetailRepository.findAllByOrderByFolderSeqAsc();
    }
}
