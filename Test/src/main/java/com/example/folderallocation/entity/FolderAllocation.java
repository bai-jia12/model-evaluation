package com.example.folderallocation.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "folder_allocation")
public class FolderAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shop;

    @Column(name = "total_links")
    private Integer totalLinks;

    @Column(name = "repeat_count")
    private Integer repeatCount;

    private Integer pattern;

    @Column(name = "folder_allocation")
    private String folderAllocation;

    @Column(name = "actual_links")
    private Integer actualLinks;

    @Column(name = "process_id")
    private String processId;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    private Integer status = 0;
}
