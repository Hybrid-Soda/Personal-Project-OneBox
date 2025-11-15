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

    // 폴더 조회 및 타입 검증
    public void validateTypeFolder(MetadataType type) {
        if (type != MetadataType.FOLDER) throw new ApplicationException(ApplicationError.IS_NOT_FOLDER);
    }

    // 동일한 이름 존재 여부 검증
    public void validateFolderName(String folderName, Long parentFolderId) {
        Boolean isExist = metadataRepository.existsByNameAndParentFolderIdAndType(folderName, parentFolderId, MetadataType.FOLDER);

        if (isExist) throw new ApplicationException(ApplicationError.DUPLICATE_FOLDER_NAME);
    }

    // 하위 폴더 개수 제한 검증
    public void validateChildFolderLimit(Long parentFolderId) {
        long count = metadataRepository.countByParentFolderIdAndType(parentFolderId, MetadataType.FOLDER);

        if (count >= 100L) throw new ApplicationException(ApplicationError.TOO_MANY_CHILD_FOLDERS);
    }

    // 경로 길이 제한 검증 - 생성용
    public void validatePathLength(String newParentFolderPath, String folderName) {
        int newPathLength = newParentFolderPath.length() + folderName.length() + 1;

        if (newPathLength > 255) throw new ApplicationException(ApplicationError.PATH_LENGTH_EXCEEDED);
    }

    // 경로 길이 제한 검증 - 수정용
    public void validatePathLengthForUpdate(
            Long ownerId, String newParentFolderPath, String oldFolderPath, String folderName) {
        String longestChildPath = metadataRepository.findLongestChildPath(ownerId, oldFolderPath);
        String relative = longestChildPath.substring(oldFolderPath.length());
        int newPathLength = newParentFolderPath.length() + folderName.length() + 1 + relative.length();

        if (newPathLength > 255) throw new ApplicationException(ApplicationError.PATH_LENGTH_EXCEEDED);
    }

    // 자신의 하위 폴더 이동 검증
    public void validateRecursion(Long parentFolderId, Long nowFolderId) {
        if (parentFolderId.equals(nowFolderId)) {
            throw new ApplicationException(ApplicationError.MOVE_TO_ITSELF_NOT_ALLOWED);
        }
    }

    // 루트 폴더 이동 검증
    public void validateRootFolderUpdate(Metadata folder) {
        if (folder.getParentFolder() == null) {
            throw new ApplicationException(ApplicationError.ROOT_FOLDER_UPDATE_NOT_ALLOWED);
        }
    }
}
