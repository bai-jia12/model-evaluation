package com.example.folderallocation.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "folder_allocation_shop")
public class FolderAllocationShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_detail_id")
    private Long folderDetailId;

    @Column(name = "folder_name")
    private String folderName;

    private String shop;

    private Integer pattern;

    @Column(name = "process_id")
    private String processId;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    private Integer status = 0;
}
