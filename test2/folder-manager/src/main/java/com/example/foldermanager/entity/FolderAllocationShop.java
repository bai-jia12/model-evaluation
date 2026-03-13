package com.example.foldermanager.entity;

import javax.persistence.*;
import lombok.Data;
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
    
    @Column(name = "shop")
    private String shop;
    
    @Column(name = "color")
    private Integer color;
    
    @Column(name = "process_id", length = 36)
    private String processId;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "status")
    private Integer status = 0;
}
