package com.example.foldermanager.repository;

import com.example.foldermanager.entity.FolderAllocationShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderAllocationShopRepository extends JpaRepository<FolderAllocationShop, Long> {
    
    List<FolderAllocationShop> findByProcessId(String processId);
    
    @Query("SELECT fas FROM FolderAllocationShop fas WHERE fas.folderName = :folderName AND fas.shop = :shop AND fas.status = 1")
    List<FolderAllocationShop> findConfirmedByFolderNameAndShop(@Param("folderName") String folderName, @Param("shop") String shop);
    
    @Query("SELECT COUNT(fas) FROM FolderAllocationShop fas WHERE fas.folderDetailId = :folderDetailId AND fas.color = :color AND fas.status = 1")
    int countConfirmedByFolderAndColor(@Param("folderDetailId") Long folderDetailId, @Param("color") Integer color);
}
