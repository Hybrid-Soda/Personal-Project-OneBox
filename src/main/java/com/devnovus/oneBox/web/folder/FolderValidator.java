package com.devnovus.oneBox.web.folder;

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
    private static final int MAX_PATH_LENGTH = 255;
    private static final int MAX_CHILD_FOLDERS = 100;

    private final MetadataRepository metadataRepository;

    /** 폴더 타입 여부 검증 */
    public void validateFolderType(MetadataType type) {
        if (type != MetadataType.FOLDER) {
            throw new ApplicationException(ApplicationError.NOT_A_FOLDER);
        }
    }

    /** 동일 부모 내 폴더 이름 중복 검증 */
    public void validateDuplicatedName(String name, Long parentId) {
        boolean exists = metadataRepository.existsByNameAndParentFolderIdAndType(
                name, parentId, MetadataType.FOLDER
        );

        if (exists) {
            throw new ApplicationException(ApplicationError.FOLDER_NAME_DUPLICATED);
        }
    }

    /** 폴더 최대 생성 개수 검증 */
    public void validateChildFolderLimit(Long parentId) {
        long count = metadataRepository.countByParentFolderIdAndType(parentId, MetadataType.FOLDER);

        if (count >= MAX_CHILD_FOLDERS) {
            throw new ApplicationException(ApplicationError.FOLDER_CHILD_LIMIT_EXCEEDED);
        }
    }

    /** 경로 길이 검증 (생성 시) */
    public void validatePathLength(String parentPath, String newFolderName) {
        int length = calculateNewPathLength(parentPath, newFolderName);

        if (length > MAX_PATH_LENGTH) {
            throw new ApplicationException(ApplicationError.FOLDER_PATH_LENGTH_EXCEEDED);
        }
    }

    /** 경로 길이 검증 (수정/이동 시) */
    public void validatePathLengthForUpdate(
            Long ownerId, String targetParentPath, String oldPath, String newFolderName
    ) {
        String longestChildPath = metadataRepository.findLongestChildPath(ownerId, oldPath);
        int relativeLength = longestChildPath.substring(oldPath.length()).length();
        int length = calculateNewPathLength(targetParentPath, newFolderName) + relativeLength;

        if (length > MAX_PATH_LENGTH) {
            throw new ApplicationException(ApplicationError.FOLDER_PATH_LENGTH_EXCEEDED);
        }
    }

    /** 자식으로 이동하는 순환 방지 */
    public void validateNoCircularMove(Long parentFolderId, Long currentFolderId) {
        if (parentFolderId.equals(currentFolderId)) {
            throw new ApplicationException(ApplicationError.FOLDER_CANNOT_MOVE_TO_DESCENDANT);
        }
    }

    /** 루트 폴더 수정 금지 */
    public void validateRootFolderUpdate(Metadata folder) {
        if (folder.getParentFolder() == null) {
            throw new ApplicationException(ApplicationError.FOLDER_NOT_ALLOWED_ROOT_MODIFY);
        }
    }

    /** 공통 경로 계산 로직 */
    private int calculateNewPathLength(String parentPath, String folderName) {
        return parentPath.length() + folderName.length() + 1; // '/' 포함
    }
}
