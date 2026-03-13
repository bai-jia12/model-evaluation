package com.example.foldermanager.service;

import com.example.foldermanager.dto.AllocationCondition;
import com.example.foldermanager.entity.FolderAllocation;
import com.example.foldermanager.entity.FolderAllocationShop;
import com.example.foldermanager.entity.FolderDetail;
import com.example.foldermanager.repository.FolderAllocationRepository;
import com.example.foldermanager.repository.FolderAllocationShopRepository;
import com.example.foldermanager.repository.FolderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FolderAllocationService {
    
    @Autowired
    private FolderDetailRepository folderDetailRepository;
    
    @Autowired
    private FolderAllocationRepository folderAllocationRepository;
    
    @Autowired
    private FolderAllocationShopRepository folderAllocationShopRepository;
    
    @Transactional
    public Map<String, Object> executeAllocation(List<AllocationCondition> conditions, String processId) {
        List<FolderDetail> allFolders = folderDetailRepository.findAllOrderByFolderSeqAsc();
        int successCount = 0;
        int failCount = 0;
        
        for (AllocationCondition condition : conditions) {
            boolean result = processSingleCondition(condition, processId, allFolders);
            if (result) {
                successCount++;
            } else {
                failCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("processId", processId);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalConditions", conditions.size());
        return result;
    }
    
    private boolean processSingleCondition(AllocationCondition condition, String processId, List<FolderDetail> allFolders) {
        List<FolderDetail> availableFolders = filterAvailableFolders(condition, allFolders);
        
        if (availableFolders.isEmpty()) {
            return false;
        }
        
        List<FolderDetail> selectedFolders = selectFoldersForCondition(condition, availableFolders);
        
        if (selectedFolders.isEmpty()) {
            return false;
        }
        
        saveAllocationRecords(condition, selectedFolders, processId);
        return true;
    }
    
    private List<FolderDetail> filterAvailableFolders(AllocationCondition condition, List<FolderDetail> allFolders) {
        List<FolderDetail> result = new ArrayList<>();
        
        for (FolderDetail folder : allFolders) {
            if (!isFolderAvailable(folder, condition.getShop(), condition.getColor())) {
                continue;
            }
            result.add(folder);
        }
        
        return result;
    }
    
    private boolean isFolderAvailable(FolderDetail folder, String shop, Integer color) {
        List<FolderAllocationShop> used = folderAllocationShopRepository
            .findConfirmedByFolderNameAndShop(folder.getName(), shop);
        if (!used.isEmpty()) {
            return false;
        }
        
        int usedColorCount = folderAllocationShopRepository
            .countConfirmedByFolderAndColor(folder.getId(), color);
        if (usedColorCount > 0) {
            return false;
        }
        
        return true;
    }
    
    private List<FolderDetail> selectFoldersForCondition(AllocationCondition condition, List<FolderDetail> availableFolders) {
        List<FolderDetail> selected = new ArrayList<>();
        int accumulatedLinks = 0;
        int targetLinks = condition.getLinkTotal();
        int repeatCount = condition.getRepeatCount();
        int color = condition.getColor();
        
        for (FolderDetail folder : availableFolders) {
            int linksPerFolder = (int) Math.floor((double) folder.getImageCount() * repeatCount / color);
            
            if (accumulatedLinks + linksPerFolder >= targetLinks && !selected.isEmpty()) {
                break;
            }
            
            selected.add(folder);
            accumulatedLinks += linksPerFolder;
        }
        
        return selected;
    }
    
    private void saveAllocationRecords(AllocationCondition condition, List<FolderDetail> selectedFolders, String processId) {
        String folderAllocation = selectedFolders.stream()
            .map(f -> String.valueOf(f.getFolderSeq()))
            .collect(Collectors.joining(","));
        
        int actualLinkCount = 0;
        for (FolderDetail folder : selectedFolders) {
            int linksPerFolder = (int) Math.floor((double) folder.getImageCount() * condition.getRepeatCount() / condition.getColor());
            actualLinkCount += linksPerFolder;
        }
        
        FolderAllocation allocation = new FolderAllocation();
        allocation.setShop(condition.getShop());
        allocation.setLinkTotal(condition.getLinkTotal());
        allocation.setRepeatCount(condition.getRepeatCount());
        allocation.setColor(condition.getColor());
        allocation.setFolderAllocation(folderAllocation);
        allocation.setActualLinkCount(actualLinkCount);
        allocation.setProcessId(processId);
        allocation.setCreateTime(new Date());
        allocation.setStatus(0);
        folderAllocationRepository.save(allocation);
        
        for (FolderDetail folder : selectedFolders) {
            FolderAllocationShop shopRecord = new FolderAllocationShop();
            shopRecord.setFolderDetailId(folder.getId());
            shopRecord.setFolderName(folder.getName());
            shopRecord.setShop(condition.getShop());
            shopRecord.setColor(condition.getColor());
            shopRecord.setProcessId(processId);
            shopRecord.setCreateTime(new Date());
            shopRecord.setStatus(0);
            folderAllocationShopRepository.save(shopRecord);
        }
    }
    
    @Transactional
    public boolean confirmAllocation(String processId) {
        List<FolderAllocation> allocations = folderAllocationRepository.findByProcessId(processId);
        List<FolderAllocationShop> shopRecords = folderAllocationShopRepository.findByProcessId(processId);
        
        if (allocations.isEmpty() && shopRecords.isEmpty()) {
            return false;
        }
        
        for (FolderAllocation allocation : allocations) {
            allocation.setStatus(1);
            folderAllocationRepository.save(allocation);
        }
        
        for (FolderAllocationShop shopRecord : shopRecords) {
            shopRecord.setStatus(1);
            folderAllocationShopRepository.save(shopRecord);
        }
        
        return true;
    }
    
    public List<FolderAllocation> getAllocationsByProcessId(String processId) {
        return folderAllocationRepository.findByProcessId(processId);
    }
    
    public List<FolderAllocationShop> getShopRecordsByProcessId(String processId) {
        return folderAllocationShopRepository.findByProcessId(processId);
    }
}
