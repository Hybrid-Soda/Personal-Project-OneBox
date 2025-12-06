package com.devnovus.oneBox.domain.folder.util;

import com.devnovus.oneBox.domain.folder.dto.FolderRenameRequest;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.devnovus.oneBox.global.constant.CommonConstant.MAX_CHILD_FOLDERS;
import static com.devnovus.oneBox.global.constant.CommonConstant.MAX_PATH_LENGTH;

@Component
@RequiredArgsConstructor
public class FolderValidator {
    private final MetadataRepository metadataRepository;

    /** 폴더 생성 시 검증 */
    public void validateForCreate(Metadata parentFolder, String folderName) {
        validateFolderType(parentFolder.getType());
        validateDuplicatedName(folderName, parentFolder.getId());
        validateChildFolderLimit(parentFolder.getId());
    }

    /** 폴더 이동 시 검증 */
    public void validateForMove(Metadata parentFolder, Metadata folder) {
        validateFolderType(parentFolder.getType());
        validateRootFolderUpdate(folder);
        validateNoCircularMove(parentFolder.getId(), folder.getId());
        validateChildFolderLimit(parentFolder.getId());
    }

    /** 폴더이름수정 시 검증 */
    public void validateForRename(Metadata parentFolder, Metadata folder, FolderRenameRequest req) {
        validateFolderType(parentFolder.getType());
        validateRootFolderUpdate(folder);
        validateDuplicatedName(req.getFolderName(), parentFolder.getId());
    }

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

    /** 자식으로 이동하는 순환 방지 */
    public void validateNoCircularMove(Long newParentId, Long folderId) {
        if (newParentId < folderId) {
            throw new ApplicationException(ApplicationError.FOLDER_CANNOT_MOVE_TO_DESCENDANT);
        }
    }

    /** 루트 폴더 수정 금지 */
    public void validateRootFolderUpdate(Metadata folder) {
        if (folder.getParentFolder() == null) {
            throw new ApplicationException(ApplicationError.FOLDER_NOT_ALLOWED_ROOT_MODIFY);
        }
    }
}
