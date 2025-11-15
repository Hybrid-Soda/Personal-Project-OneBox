package com.devnovus.oneBox.web.common;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.Metadata;
import com.devnovus.oneBox.domain.MetadataRepository;
import com.devnovus.oneBox.domain.MetadataType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FolderValidator {
    private final MetadataRepository metadataRepository;

    // 메타데이터 타입 검증
    public void validateTypeFolder(MetadataType type) {
        if (type != MetadataType.FOLDER) throw new ApplicationException(ApplicationError.NOT_A_FOLDER);
    }

    // 중복된 이름 검증
    public void validateFolderName(String folderName, Long parentFolderId) {
        Boolean isExist = metadataRepository.existsByNameAndParentFolderIdAndType(folderName, parentFolderId, MetadataType.FOLDER);

        if (isExist) throw new ApplicationException(ApplicationError.FOLDER_NAME_DUPLICATED);
    }

    // 하위 폴더 수 제한 검증
    public void validateChildFolderLimit(Long parentFolderId) {
        long count = metadataRepository.countByParentFolderIdAndType(parentFolderId, MetadataType.FOLDER);

        if (count >= 100L) throw new ApplicationException(ApplicationError.FOLDER_CHILD_LIMIT_EXCEEDED);
    }

    // 경로 길이 제한 검증 - 생성용
    public void validatePathLength(String parentFolderPath, String folderName) {
        int newPathLength = parentFolderPath.length() + folderName.length() + 1;

        if (newPathLength > 255) throw new ApplicationException(ApplicationError.FOLDER_PATH_LENGTH_EXCEEDED);
    }

    // 경로 길이 제한 검증 - 수정용
    public void validatePathLengthForUpdate(Long ownerId, String parentFolderPath, String oldFolderPath, String folderName) {
        String longestChildPath = metadataRepository.findLongestChildPath(ownerId, oldFolderPath);
        String relative = longestChildPath.substring(oldFolderPath.length());
        int newPathLength = parentFolderPath.length() + folderName.length() + 1 + relative.length();

        if (newPathLength > 255) throw new ApplicationException(ApplicationError.FOLDER_PATH_LENGTH_EXCEEDED);
    }

    // 순환 구조 여부 검증
    public void validateRecursion(Long parentFolderId, Long nowFolderId) {
        if (parentFolderId.equals(nowFolderId)) {
            throw new ApplicationException(ApplicationError.FOLDER_CANNOT_MOVE_TO_DESCENDANT);
        }
    }

    // 루트 폴더 수정 검증
    public void validateRootFolderUpdate(Metadata folder) {
        if (folder.getParentFolder() == null) {
            throw new ApplicationException(ApplicationError.FOLDER_NOT_ALLOWED_ROOT_MODIFY);
        }
    }
}
