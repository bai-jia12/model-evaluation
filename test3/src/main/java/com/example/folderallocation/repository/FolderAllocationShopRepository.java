package com.example.folderallocation.repository;

import com.example.folderallocation.entity.FolderAllocationShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderAllocationShopRepository extends JpaRepository<FolderAllocationShop, Long> {

    List<FolderAllocationShop> findByProcessId(String processId);

    List<FolderAllocationShop> findByProcessIdAndStatus(String processId, Integer status);

    List<FolderAllocationShop> findByShopAndStatus(String shop, Integer status);

    List<FolderAllocationShop> findByFolderDetailIdAndPatternNoAndStatus(Long folderDetailId, Integer patternNo, Integer status);

    @Modifying
    @Query("UPDATE FolderAllocationShop fas SET fas.status = :status WHERE fas.processId = :processId")
    int updateStatusByProcessId(@Param("processId") String processId, @Param("status") Integer status);

    @Query("SELECT COUNT(fas) FROM FolderAllocationShop fas WHERE fas.folderDetailId = :folderDetailId AND fas.patternNo = :patternNo AND fas.status = 1")
    long countUsedPatternsByFolderAndPattern(@Param("folderDetailId") Long folderDetailId, @Param("patternNo") Integer patternNo);
}
