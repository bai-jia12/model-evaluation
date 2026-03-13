package com.example.folderallocation.controller;

import com.example.folderallocation.dto.AllocationCondition;
import com.example.folderallocation.dto.AllocationResult;
import com.example.folderallocation.entity.FolderAllocationDetail;
import com.example.folderallocation.entity.FolderDetail;
import com.example.folderallocation.service.AllocationService;
import com.example.folderallocation.service.FolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private AllocationService allocationService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("folderCount", folderService.getFolderCount());
        return "index";
    }

    @PostMapping("/api/folders/scan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanFolders() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<FolderDetail> folders = folderService.scanAndSaveFolders();
            result.put("success", true);
            result.put("message", "成功扫描并保存 " + folders.size() + " 个文件夹");
            result.put("count", folders.size());
        } catch (Exception e) {
            log.error("扫描文件夹时出错: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "扫描失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/folders")
    @ResponseBody
    public ResponseEntity<List<FolderDetail>> getAllFolders() {
        return ResponseEntity.ok(folderService.getAllFolders());
    }

    @PostMapping("/api/allocation/start")
    @ResponseBody
    public ResponseEntity<AllocationResult> startAllocation(@RequestBody List<AllocationCondition> conditions) {
        String processId = generateProcessId();
        log.info("开始分配，流程ID: {}, 条件数量: {}", processId, conditions.size());
        
        AllocationResult result;
        
        try {
            result = allocationService.allocateFolders(conditions, processId);
        } catch (Exception e) {
            log.error("分配过程中出错: {}", e.getMessage(), e);
            result = new AllocationResult();
            result.setProcessId(processId);
            result.setSuccess(false);
            result.setMessage("分配失败: " + e.getMessage());
            result.setDetails(new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/allocation/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmAllocation(@RequestParam String processId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = allocationService.confirmAllocation(processId);
        result.put("success", success);
        result.put("message", success ? "确认成功" : "确认失败");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/allocation/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelAllocation(@RequestParam String processId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = allocationService.cancelAllocation(processId);
        result.put("success", success);
        result.put("message", success ? "撤销成功" : "撤销失败");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/allocation/details")
    @ResponseBody
    public ResponseEntity<List<FolderAllocationDetail>> getAllocationDetails(@RequestParam String processId) {
        return ResponseEntity.ok(allocationService.getAllocationDetailsByProcessId(processId));
    }

    private String generateProcessId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 36);
    }
}
