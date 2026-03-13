package com.example.folderallocation.service;

import com.example.folderallocation.dto.AllocationCondition;
import com.example.folderallocation.dto.AllocationResult;
import com.example.folderallocation.entity.FolderAllocationDetail;
import com.example.folderallocation.entity.FolderAllocationShop;
import com.example.folderallocation.entity.FolderDetail;
import com.example.folderallocation.repository.FolderAllocationDetailRepository;
import com.example.folderallocation.repository.FolderAllocationShopRepository;
import com.example.folderallocation.repository.FolderDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AllocationService {

    @Autowired
    private FolderDetailRepository folderDetailRepository;

    @Autowired
    private FolderAllocationDetailRepository allocationDetailRepository;

    @Autowired
    private FolderAllocationShopRepository allocationShopRepository;

    @Transactional
    public AllocationResult allocateFolders(List<AllocationCondition> conditions, String processId) {
        log.info("开始分配文件夹，流程ID: {}, 条件数量: {}", processId, conditions.size());
        
        AllocationResult result = new AllocationResult();
        result.setProcessId(processId);
        result.setDetails(new ArrayList<>());
        result.setSuccess(true);

        for (AllocationCondition condition : conditions) {
            AllocationResult.AllocationDetailResult detailResult = processSingleCondition(condition, processId);
            result.getDetails().add(detailResult);
            
            if (!detailResult.isSuccess()) {
                result.setSuccess(false);
                result.setMessage("部分条件分配失败");
            }
        }

        if (result.getMessage() == null) {
            result.setMessage("分配成功，请确认");
        }

        log.info("文件夹分配完成，流程ID: {}", processId);
        return result;
    }

    private AllocationResult.AllocationDetailResult processSingleCondition(AllocationCondition condition, String processId) {
        AllocationResult.AllocationDetailResult result = new AllocationResult.AllocationDetailResult();
        result.setShop(condition.getShop());
        result.setTotalLinks(condition.getTotalLinks());
        result.setRepeatCount(condition.getRepeatCount());
        result.setPatternNo(condition.getPatternNo());
        result.setAllocatedFolderNos(new ArrayList<>());
        result.setSuccess(false);

        try {
            List<FolderDetail> allFolders = folderDetailRepository.findAllByOrderByFolderNoAsc();
            List<FolderDetail> availableFolders = filterAvailableFolders(
                    allFolders, condition.getShop(), condition.getPatternNo());

            if (availableFolders.isEmpty()) {
                result.setMessage("没有可用的文件夹");
                return result;
            }

            List<FolderDetail> selectedFolders = selectFolders(availableFolders, condition);
            
            if (selectedFolders.isEmpty()) {
                result.setMessage("无法满足链接总数要求");
                return result;
            }

            int actualLinkCount = calculateActualLinkCount(selectedFolders, condition);
            
            saveAllocationRecords(selectedFolders, condition, processId, actualLinkCount);

            result.setAllocatedFolderNos(selectedFolders.stream()
                    .map(FolderDetail::getFolderNo)
                    .collect(Collectors.toList()));
            result.setActualLinkCount(actualLinkCount);
            result.setSuccess(true);
            result.setMessage("分配成功");

        } catch (Exception e) {
            log.error("处理条件时出错: {}", e.getMessage(), e);
            result.setMessage("处理失败: " + e.getMessage());
        }

        return result;
    }

    private List<FolderDetail> filterAvailableFolders(List<FolderDetail> allFolders, String shop, Integer patternNo) {
        List<FolderDetail> availableFolders = new ArrayList<>();
        
        List<FolderAllocationShop> usedByShop = allocationShopRepository.findByShopAndStatus(shop, FolderAllocationShop.STATUS_CONFIRMED);
        List<Long> usedFolderIds = usedByShop.stream()
                .map(FolderAllocationShop::getFolderDetailId)
                .distinct()
                .collect(Collectors.toList());
        List<String> usedFolderNames = usedByShop.stream()
                .map(FolderAllocationShop::getFolderName)
                .distinct()
                .collect(Collectors.toList());
        
        for (FolderDetail folder : allFolders) {
            if (usedFolderIds.contains(folder.getId())) {
                continue;
            }
            if (usedFolderNames.contains(folder.getName())) {
                continue;
            }
            
            long patternCount = allocationShopRepository.countUsedPatternsByFolderAndPattern(folder.getId(), patternNo);
            if (patternCount >= 12) {
                continue;
            }
            
            availableFolders.add(folder);
        }
        
        return availableFolders;
    }

    private List<FolderDetail> selectFolders(List<FolderDetail> availableFolders, AllocationCondition condition) {
        List<FolderDetail> selectedFolders = new ArrayList<>();
        int totalCalculatedLinks = 0;
        int targetLinks = condition.getTotalLinks();
        int patternCount = condition.getPatternNo();
        int repeatCount = condition.getRepeatCount();

        for (FolderDetail folder : availableFolders) {
            int folderLinks = (folder.getImageCount() * repeatCount) / patternCount;
            
            if (folderLinks <= 0) {
                continue;
            }

            selectedFolders.add(folder);
            totalCalculatedLinks += folderLinks;

            if (totalCalculatedLinks >= targetLinks) {
                break;
            }
        }

        if (totalCalculatedLinks < targetLinks) {
            log.warn("无法满足链接总数要求，已选文件夹链接数: {}, 目标: {}", totalCalculatedLinks, targetLinks);
            return new ArrayList<>();
        }

        if (totalCalculatedLinks > targetLinks && selectedFolders.size() > 1) {
            selectedFolders.remove(selectedFolders.size() - 1);
        }

        return selectedFolders;
    }

    private int calculateActualLinkCount(List<FolderDetail> selectedFolders, AllocationCondition condition) {
        int total = 0;
        for (FolderDetail folder : selectedFolders) {
            total += (folder.getImageCount() * condition.getRepeatCount()) / condition.getPatternNo();
        }
        return total;
    }

    private void saveAllocationRecords(List<FolderDetail> selectedFolders, AllocationCondition condition, 
                                       String processId, int actualLinkCount) {
        String folderAllocationStr = selectedFolders.stream()
                .map(f -> String.valueOf(f.getFolderNo()))
                .collect(Collectors.joining(","));

        FolderAllocationDetail allocationDetail = new FolderAllocationDetail();
        allocationDetail.setShop(condition.getShop());
        allocationDetail.setTotalLinks(condition.getTotalLinks());
        allocationDetail.setRepeatCount(condition.getRepeatCount());
        allocationDetail.setPatternNo(condition.getPatternNo());
        allocationDetail.setFolderAllocation(folderAllocationStr);
        allocationDetail.setActualLinkCount(actualLinkCount);
        allocationDetail.setProcessId(processId);
        allocationDetail.setStatus(FolderAllocationDetail.STATUS_UNCONFIRMED);
        allocationDetailRepository.save(allocationDetail);

        for (FolderDetail folder : selectedFolders) {
            FolderAllocationShop shopRecord = new FolderAllocationShop();
            shopRecord.setFolderDetailId(folder.getId());
            shopRecord.setFolderName(folder.getName());
            shopRecord.setShop(condition.getShop());
            shopRecord.setPatternNo(condition.getPatternNo());
            shopRecord.setProcessId(processId);
            shopRecord.setStatus(FolderAllocationShop.STATUS_UNCONFIRMED);
            allocationShopRepository.save(shopRecord);
        }
    }

    @Transactional
    public boolean confirmAllocation(String processId) {
        log.info("确认分配，流程ID: {}", processId);
        try {
            allocationDetailRepository.updateStatusByProcessId(processId, FolderAllocationDetail.STATUS_CONFIRMED);
            allocationShopRepository.updateStatusByProcessId(processId, FolderAllocationShop.STATUS_CONFIRMED);
            return true;
        } catch (Exception e) {
            log.error("确认分配时出错: {}", e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public boolean cancelAllocation(String processId) {
        log.info("撤销分配，流程ID: {}", processId);
        try {
            allocationDetailRepository.updateStatusByProcessId(processId, FolderAllocationDetail.STATUS_CANCELLED);
            allocationShopRepository.updateStatusByProcessId(processId, FolderAllocationShop.STATUS_CANCELLED);
            return true;
        } catch (Exception e) {
            log.error("撤销分配时出错: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<FolderAllocationDetail> getAllocationDetailsByProcessId(String processId) {
        return allocationDetailRepository.findByProcessId(processId);
    }
}
