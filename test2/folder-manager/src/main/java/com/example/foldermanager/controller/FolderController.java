package com.example.foldermanager.controller;

import com.example.foldermanager.dto.AllocationRequest;
import com.example.foldermanager.entity.FolderAllocation;
import com.example.foldermanager.entity.FolderAllocationShop;
import com.example.foldermanager.entity.FolderDetail;
import com.example.foldermanager.service.FolderAllocationService;
import com.example.foldermanager.service.FolderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class FolderController {
    
    @Autowired
    private FolderDetailService folderDetailService;
    
    @Autowired
    private FolderAllocationService folderAllocationService;
    
    @GetMapping("/folders")
    public Map<String, Object> getAllFolders() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<FolderDetail> folders = folderDetailService.getAllFolders();
            result.put("success", true);
            result.put("data", folders);
            result.put("total", folders.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    @PostMapping("/folders/scan")
    public Map<String, Object> scanFolders() {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = folderDetailService.scanAndSaveFolders();
            result.put("success", true);
            result.put("message", "成功扫描并保存 " + count + " 个文件夹");
            result.put("count", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "扫描失败: " + e.getMessage());
        }
        return result;
    }
    
    @PostMapping("/allocation/start")
    public Map<String, Object> startAllocation(@RequestBody AllocationRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String processId = UUID.randomUUID().toString();
            Map<String, Object> allocationResult = folderAllocationService.executeAllocation(request.getConditions(), processId);
            result.put("success", true);
            result.put("data", allocationResult);
            result.put("message", "分配完成，等待确认");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "分配失败: " + e.getMessage());
        }
        return result;
    }
    
    @PostMapping("/allocation/confirm")
    public Map<String, Object> confirmAllocation(@RequestParam String processId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean confirmed = folderAllocationService.confirmAllocation(processId);
            if (confirmed) {
                result.put("success", true);
                result.put("message", "确认成功");
            } else {
                result.put("success", false);
                result.put("message", "未找到对应的分配记录");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "确认失败: " + e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/allocation/detail")
    public Map<String, Object> getAllocationDetail(@RequestParam String processId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<FolderAllocation> allocations = folderAllocationService.getAllocationsByProcessId(processId);
            List<FolderAllocationShop> shopRecords = folderAllocationService.getShopRecordsByProcessId(processId);
            result.put("success", true);
            result.put("allocations", allocations);
            result.put("shopRecords", shopRecords);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }
}
