package com.example.folderallocation.repository;

import com.example.folderallocation.entity.FolderAllocationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderAllocationDetailRepository extends JpaRepository<FolderAllocationDetail, Long> {

    List<FolderAllocationDetail> findByProcessId(String processId);

    List<FolderAllocationDetail> findByProcessIdAndStatus(String processId, Integer status);

    @Modifying
    @Query("UPDATE FolderAllocationDetail fad SET fad.status = :status WHERE fad.processId = :processId")
    int updateStatusByProcessId(@Param("processId") String processId, @Param("status") Integer status);
}
