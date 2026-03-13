package com.example.foldermanager.entity;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "folder_detail")
public class FolderDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "folder_seq")
    private Integer folderSeq;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "image_count")
    private Integer imageCount;
    
    @Column(name = "folder_path")
    private String folderPath;
}
