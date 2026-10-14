package com.notpoggoez.File;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByFileId(String fileId);

    @Modifying
    @Transactional
    @Query("UPDATE FileMetadata f SET f.status = ?2 WHERE f.fileId = ?1")
    int updateStatusByFileId(String fileId, FileStatus status);
}