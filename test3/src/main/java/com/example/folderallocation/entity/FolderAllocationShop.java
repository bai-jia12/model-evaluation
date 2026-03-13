package com.example.folderallocation.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "folder_allocation_shop")
public class FolderAllocationShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_detail_id", nullable = false)
    private Long folderDetailId;

    @Column(name = "folder_name", nullable = false)
    private String folderName;

    @Column(name = "shop", nullable = false)
    private String shop;

    @Column(name = "pattern_no", nullable = false)
    private Integer patternNo;

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
