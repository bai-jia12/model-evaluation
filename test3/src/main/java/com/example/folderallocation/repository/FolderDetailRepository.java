package com.example.folderallocation.repository;

import com.example.folderallocation.entity.FolderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderDetailRepository extends JpaRepository<FolderDetail, Long> {

    Optional<FolderDetail> findByFolderNo(Integer folderNo);

    List<FolderDetail> findAllByOrderByFolderNoAsc();

    @Query("SELECT fd FROM FolderDetail fd WHERE fd.folderNo NOT IN " +
           "(SELECT fas.folderDetailId FROM FolderAllocationShop fas WHERE fas.shop = :shop AND fas.status = 1) " +
           "AND fd.name NOT IN " +
           "(SELECT fas2.folderName FROM FolderAllocationShop fas2 WHERE fas2.shop = :shop AND fas2.status = 1) " +
           "AND fd.id NOT IN " +
           "(SELECT fas3.folderDetailId FROM FolderAllocationShop fas3 WHERE fas3.patternNo = :patternNo AND fas3.status = 1 GROUP BY fas3.folderDetailId HAVING COUNT(fas3.id) >= 12) " +
           "ORDER BY fd.folderNo ASC")
    List<FolderDetail> findAvailableFolders(@Param("shop") String shop, @Param("patternNo") Integer patternNo);

    @Query(value = "SELECT fd.* FROM folder_detail fd WHERE fd.folder_no NOT IN " +
           "(SELECT fas.folder_detail_id FROM folder_allocation_shop fas WHERE fas.shop = ?1 AND fas.status = 1) " +
           "AND fd.name NOT IN " +
           "(SELECT fas2.folder_name FROM folder_allocation_shop fas2 WHERE fas2.shop = ?1 AND fas2.status = 1) " +
           "AND fd.id NOT IN " +
           "(SELECT fas3.folder_detail_id FROM folder_allocation_shop fas3 WHERE fas3.pattern_no = ?2 AND fas3.status = 1 GROUP BY fas3.folder_detail_id HAVING COUNT(fas3.id) >= 12) " +
           "ORDER BY fd.folder_no ASC", nativeQuery = true)
    List<FolderDetail> findAvailableFoldersNative(String shop, Integer patternNo);
}
