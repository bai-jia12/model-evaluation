package com.example.folderallocation.repository;

import com.example.folderallocation.entity.FolderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderDetailRepository extends JpaRepository<FolderDetail, Long> {

    Optional<FolderDetail> findByFolderSeq(Integer folderSeq);

    List<FolderDetail> findAllByOrderByFolderSeqAsc();
}
