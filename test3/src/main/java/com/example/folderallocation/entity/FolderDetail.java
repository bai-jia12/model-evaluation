package com.example.folderallocation.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "folder_detail")
public class FolderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_no", nullable = false, unique = true)
    private Integer folderNo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_count", nullable = false)
    private Integer imageCount;

    @Column(name = "folder_path", nullable = false)
    private String folderPath;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
