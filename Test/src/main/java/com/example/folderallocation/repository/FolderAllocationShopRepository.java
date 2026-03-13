package com.example.folderallocation.repository;

import com.example.folderallocation.entity.FolderAllocationShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderAllocationShopRepository extends JpaRepository<FolderAllocationShop, Long> {

    List<FolderAllocationShop> findByProcessId(String processId);

    List<FolderAllocationShop> findByShopAndStatus(String shop, Integer status);

    List<FolderAllocationShop> findByFolderDetailIdAndStatus(Long folderDetailId, Integer status);

    @Query("SELECT COUNT(DISTINCT fas.pattern) FROM FolderAllocationShop fas WHERE fas.folderDetailId = ?1 AND fas.status = 1")
    Integer countUsedPatternsByFolderDetailId(Long folderDetailId);

    @Query("SELECT DISTINCT fas.folderName FROM FolderAllocationShop fas WHERE fas.shop = ?1 AND fas.status = 1")
    List<String> findUsedFolderNamesByShopAndStatus(String shop, Integer status);
}
