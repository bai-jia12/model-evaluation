package com.example.folderallocation.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "folder_allocation_detail")
public class FolderAllocationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop", nullable = false)
    private String shop;

    @Column(name = "total_links", nullable = false)
    private Integer totalLinks;

    @Column(name = "repeat_count", nullable = false)
    private Integer repeatCount;

    @Column(name = "pattern_no", nullable = false)
    private Integer patternNo;

    @Column(name = "folder_allocation")
    private String folderAllocation;

    @Column(name = "actual_link_count")
    private Integer actualLinkCount;

    @Column(name = "process_id", nullable = false)
    private String processId;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "status", nullable = false)
    private Integer status;

    public static final int STATUS_UNCONFIRMED = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_CANCELLED = 2;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        if (status == null) {
            status = STATUS_UNCONFIRMED;
        }
    }
}
