package com.example.folderallocation.dto;

import lombok.Data;

import java.util.List;

@Data
public class AllocationResult {
    private boolean success;
    private String message;
    private String processId;
    private List<AllocationDetailResult> details;

    @Data
    public static class AllocationDetailResult {
        private String shop;
        private Integer totalLinks;
        private Integer repeatCount;
        private Integer patternNo;
        private List<Integer> allocatedFolderNos;
        private Integer actualLinkCount;
        private boolean success;
        private String message;
    }
}
