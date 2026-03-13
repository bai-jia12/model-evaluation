package com.example.folderallocation.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "folder_detail")
public class FolderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_seq")
    private Integer folderSeq;

    private String name;

    @Column(name = "pic_count")
    private Integer picCount;

    @Column(name = "folder_path")
    private String folderPath;
}
