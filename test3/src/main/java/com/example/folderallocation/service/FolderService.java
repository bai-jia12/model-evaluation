package com.example.folderallocation.service;

import com.example.folderallocation.entity.FolderDetail;
import com.example.folderallocation.repository.FolderDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FolderService {

    @Autowired
    private FolderDetailRepository folderDetailRepository;

    private static final String BASE_PATH = "d:\\datapicture";
    private static final Pattern FOLDER_NAME_PATTERN = Pattern.compile("^(\\d+)-(.+?)-(\\d+)$");

    @Transactional
    public List<FolderDetail> scanAndSaveFolders() {
        log.info("开始扫描文件夹: {}", BASE_PATH);
        List<FolderDetail> result = new ArrayList<>();
        
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            log.error("文件夹路径不存在或不是目录: {}", BASE_PATH);
            return result;
        }

        File[] folders = baseDir.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            log.warn("没有找到任何文件夹");
            return result;
        }

        log.info("找到 {} 个文件夹", folders.length);

        for (File folder : folders) {
            try {
                FolderDetail folderDetail = parseFolderName(folder);
                if (folderDetail != null) {
                    saveOrUpdateFolder(folderDetail);
                    result.add(folderDetail);
                }
            } catch (Exception e) {
                log.error("处理文件夹时出错: {}, 错误: {}", folder.getName(), e.getMessage());
            }
        }

        log.info("成功处理 {} 个文件夹", result.size());
        return result;
    }

    private FolderDetail parseFolderName(File folder) {
        String folderName = folder.getName();
        Matcher matcher = FOLDER_NAME_PATTERN.matcher(folderName);
        
        if (matcher.matches()) {
            FolderDetail detail = new FolderDetail();
            detail.setFolderNo(Integer.parseInt(matcher.group(1)));
            detail.setName(matcher.group(2));
            detail.setImageCount(Integer.parseInt(matcher.group(3)));
            detail.setFolderPath(folder.getAbsolutePath());
            return detail;
        } else {
            log.warn("文件夹名称格式不正确，跳过: {}", folderName);
            return null;
        }
    }

    private void saveOrUpdateFolder(FolderDetail folderDetail) {
        Optional<FolderDetail> existing = folderDetailRepository.findByFolderNo(folderDetail.getFolderNo());
        if (existing.isPresent()) {
            FolderDetail existingDetail = existing.get();
            existingDetail.setName(folderDetail.getName());
            existingDetail.setImageCount(folderDetail.getImageCount());
            existingDetail.setFolderPath(folderDetail.getFolderPath());
            folderDetailRepository.save(existingDetail);
            log.debug("更新文件夹信息: {}", folderDetail.getFolderNo());
        } else {
            folderDetailRepository.save(folderDetail);
            log.debug("新增文件夹信息: {}", folderDetail.getFolderNo());
        }
    }

    public List<FolderDetail> getAllFolders() {
        return folderDetailRepository.findAllByOrderByFolderNoAsc();
    }

    public long getFolderCount() {
        return folderDetailRepository.count();
    }
}
