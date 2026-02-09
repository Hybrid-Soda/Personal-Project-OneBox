package com.devnovus.oneBox.domain.folder.service;

import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.global.aop.lock.AdvisoryLock;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuotaUpdater {
    private final MetadataRepository metadataRepository;

    @AdvisoryLock(metadataId = "#metadataId")
    public void plusQuota(Long metadataId) {
        Metadata metadata = findMetadata(metadataId);
        List<Metadata> ancestor = metadataRepository.findAll();

        for (Metadata folder: ancestor) {
            folder.plusSize(metadata.getSize());
        }
    }

    @AdvisoryLock(metadataId = "#metadataId")
    public void minusQuota(Long metadataId) {
        Metadata metadata = findMetadata(metadataId);
        List<Metadata> ancestor = metadataRepository.findAll();

        for (Metadata folder: ancestor) {
            folder.minusSize(metadata.getSize());
        }
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.METADATA_NOT_FOUND));
    }
}
