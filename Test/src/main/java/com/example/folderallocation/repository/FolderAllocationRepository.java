package com.example.folderallocation.repository;

import com.example.folderallocation.entity.FolderAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderAllocationRepository extends JpaRepository<FolderAllocation, Long> {

    List<FolderAllocation> findByProcessId(String processId);

    List<FolderAllocation> findByShopAndStatus(String shop, Integer status);
}
