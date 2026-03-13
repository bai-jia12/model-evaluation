package com.example.folderallocation.service;

import com.example.folderallocation.entity.FolderAllocation;
import com.example.folderallocation.entity.FolderAllocationShop;
import com.example.folderallocation.entity.FolderDetail;
import com.example.folderallocation.repository.FolderAllocationRepository;
import com.example.folderallocation.repository.FolderAllocationShopRepository;
import com.example.folderallocation.repository.FolderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    @Autowired
    private FolderDetailRepository folderDetailRepository;

    @Autowired
    private FolderAllocationRepository folderAllocationRepository;

    @Autowired
    private FolderAllocationShopRepository folderAllocationShopRepository;

    @Transactional
    public void startAllocation(List<AllocationCondition> conditions, String processId) {
        Date now = new Date();

        for (AllocationCondition condition : conditions) {
            String shop = condition.getShop();
            int totalLinks = condition.getTotalLinks();
            int repeatCount = condition.getRepeatCount();
            int pattern = condition.getPattern();

            List<FolderAllocationShop> usedFoldersByShop = folderAllocationShopRepository.findByShopAndStatus(shop, 1);
            Set<Long> usedFolderIds = usedFoldersByShop.stream()
                    .map(FolderAllocationShop::getFolderDetailId)
                    .collect(Collectors.toSet());

            List<String> usedFolderNames = folderAllocationShopRepository.findUsedFolderNamesByShopAndStatus(shop, 1);
            Set<String> usedNames = new HashSet<>(usedFolderNames);

            List<FolderDetail> allFolders = folderDetailRepository.findAllByOrderByFolderSeqAsc();
            List<FolderDetail> availableFolders = new ArrayList<>();

            for (FolderDetail folder : allFolders) {
                if (usedFolderIds.contains(folder.getId())) {
                    continue;
                }

                if (usedNames.contains(folder.getName())) {
                    continue;
                }

                Integer usedPatterns = folderAllocationShopRepository.countUsedPatternsByFolderDetailId(folder.getId());
                if (usedPatterns != null && usedPatterns >= 12) {
                    continue;
                }

                availableFolders.add(folder);
            }

            List<FolderDetail> selectedFolders = new ArrayList<>();
            int accumulatedLinks = 0;

            for (FolderDetail folder : availableFolders) {
                int folderLinks = (folder.getPicCount() * repeatCount) / pattern;
                if (accumulatedLinks + folderLinks < totalLinks) {
                    accumulatedLinks += folderLinks;
                    selectedFolders.add(folder);
                } else {
                    break;
                }
            }

            if (!selectedFolders.isEmpty()) {
                String folderSeqStr = selectedFolders.stream()
                        .map(f -> String.valueOf(f.getFolderSeq()))
                        .collect(Collectors.joining(","));

                FolderAllocation allocation = new FolderAllocation();
                allocation.setShop(shop);
                allocation.setTotalLinks(totalLinks);
                allocation.setRepeatCount(repeatCount);
                allocation.setPattern(pattern);
                allocation.setFolderAllocation(folderSeqStr);
                allocation.setActualLinks(accumulatedLinks);
                allocation.setProcessId(processId);
                allocation.setCreateTime(now);
                allocation.setStatus(0);
                folderAllocationRepository.save(allocation);

                for (FolderDetail folder : selectedFolders) {
                    FolderAllocationShop fas = new FolderAllocationShop();
                    fas.setFolderDetailId(folder.getId());
                    fas.setFolderName(folder.getName());
                    fas.setShop(shop);
                    fas.setPattern(pattern);
                    fas.setProcessId(processId);
                    fas.setCreateTime(now);
                    fas.setStatus(0);
                    folderAllocationShopRepository.save(fas);
                }
            }
        }
    }

    @Transactional
    public void confirmAllocation(String processId) {
        List<FolderAllocation> allocations = folderAllocationRepository.findByProcessId(processId);
        for (FolderAllocation allocation : allocations) {
            allocation.setStatus(1);
            folderAllocationRepository.save(allocation);
        }

        List<FolderAllocationShop> shops = folderAllocationShopRepository.findByProcessId(processId);
        for (FolderAllocationShop shop : shops) {
            shop.setStatus(1);
            folderAllocationShopRepository.save(shop);
        }
    }

    public static class AllocationCondition {
        private String shop;
        private Integer totalLinks;
        private Integer repeatCount;
        private Integer pattern;

        public String getShop() { return shop; }
        public void setShop(String shop) { this.shop = shop; }
        public Integer getTotalLinks() { return totalLinks; }
        public void setTotalLinks(Integer totalLinks) { this.totalLinks = totalLinks; }
        public Integer getRepeatCount() { return repeatCount; }
        public void setRepeatCount(Integer repeatCount) { this.repeatCount = repeatCount; }
        public Integer getPattern() { return pattern; }
        public void setPattern(Integer pattern) { this.pattern = pattern; }
    }
}
