package com.example.folderallocation.controller;

import com.example.folderallocation.entity.FolderDetail;
import com.example.folderallocation.service.AllocationService;
import com.example.folderallocation.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private AllocationService allocationService;

    @GetMapping("/")
    public String index(Model model) {
        List<FolderDetail> folders = folderService.getAllFolders();
        model.addAttribute("folders", folders);
        return "index";
    }

    @PostMapping("/scan-folders")
    @ResponseBody
    public ResponseEntity<?> scanFolders() {
        List<FolderDetail> result = folderService.scanAndSaveFolders();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", result.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start-allocation")
    @ResponseBody
    public ResponseEntity<?> startAllocation(@RequestBody Map<String, Object> request) {
        List<Map<String, Object>> conditionsList = (List<Map<String, Object>>) request.get("conditions");
        String processId = (String) request.get("processId");

        if (processId == null || processId.isEmpty()) {
            processId = UUID.randomUUID().toString();
        }

        List<AllocationService.AllocationCondition> conditions = new java.util.ArrayList<>();
        for (Map<String, Object> cond : conditionsList) {
            AllocationService.AllocationCondition ac = new AllocationService.AllocationCondition();
            ac.setShop((String) cond.get("shop"));
            ac.setTotalLinks(Integer.valueOf(cond.get("totalLinks").toString()));
            ac.setRepeatCount(Integer.valueOf(cond.get("repeatCount").toString()));
            ac.setPattern(Integer.valueOf(cond.get("pattern").toString()));
            conditions.add(ac);
        }

        allocationService.startAllocation(conditions, processId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("processId", processId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-allocation")
    @ResponseBody
    public ResponseEntity<?> confirmAllocation(@RequestBody Map<String, String> request) {
        String processId = request.get("processId");
        allocationService.confirmAllocation(processId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate-process-id")
    @ResponseBody
    public ResponseEntity<?> generateProcessId() {
        Map<String, Object> response = new HashMap<>();
        response.put("processId", UUID.randomUUID().toString());
        return ResponseEntity.ok(response);
    }
}
