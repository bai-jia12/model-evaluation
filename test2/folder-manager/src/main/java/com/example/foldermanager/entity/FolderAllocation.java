package com.example.foldermanager.entity;

import javax.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "folder_allocation")
public class FolderAllocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "shop")
    private String shop;
    
    @Column(name = "link_total")
    private Integer linkTotal;
    
    @Column(name = "repeat_count")
    private Integer repeatCount;
    
    @Column(name = "color")
    private Integer color;
    
    @Column(name = "folder_allocation", columnDefinition = "TEXT")
    private String folderAllocation;
    
    @Column(name = "actual_link_count")
    private Integer actualLinkCount;
    
    @Column(name = "process_id", length = 36)
    private String processId;
    
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Column(name = "status")
    private Integer status = 0;
}
