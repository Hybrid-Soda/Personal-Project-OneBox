package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.*;
import com.devnovus.oneBox.web.folder.dto.CreateFolderRequest;
import com.devnovus.oneBox.web.folder.dto.UpdateFolderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderMapper folderMapper;
    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;

    /** 폴더생성 */
    @Transactional
    public void createFolder(CreateFolderRequest request) {
        User user = findUser(request.getUserId());
        Metadata parentFolder = findFolder(request.getParentFolderId());

        String FolderName = request.getFolderName();
        validToCreateFolder(parentFolder, request);

        metadataRepository.save(folderMapper.createMetadata(user, parentFolder, FolderName));
    }

    /** 폴더조회 */
    @Transactional(readOnly = true)
    public void getFolder(Long folderId) {
        // pass
    }

    /** 폴더수정 */
    @Transactional
    public void updateFolder(Long folderId, UpdateFolderRequest request) {
        // pass
    }

    /** 폴더삭제 */
    @Transactional
    public void deleteFolder(Long folderId) {
        // pass
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    private Metadata findFolder(Long folderId) {
        return metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }

    private void validToCreateFolder(Metadata parentFolder, CreateFolderRequest request) {
        // 동일한 이름의 폴더가 같은 경로에 존재하는지 확인
        if (metadataRepository.existsByNameAndParentFolderIdAndType(
                request.getFolderName(), request.getParentFolderId(), MetadataType.FOLDER
        )) {
            throw new ApplicationException(ApplicationError.DUPLICATE_FOLDER_NAME);
        }
        // parentFolderId 폴더가 맞는지 확인
        if (parentFolder.getType() != MetadataType.FOLDER) {
            throw new ApplicationException(ApplicationError.IS_NOT_FOLDER);
        }
        // 같은 폴더 내에 100개 이상의 폴더가 있는지 확인
        if (metadataRepository.countByParentFolderId(parentFolder.getId()) >= 100L) {
            throw new ApplicationException(ApplicationError.TOO_MANY_CHILD_FOLDERS);
        }
        // 경로 문자열 길이가 255를 초과하는지 확인
        if (parentFolder.getPath().length() + request.getFolderName().length() > 254) {
            throw new ApplicationException(ApplicationError.PATH_LENGTH_EXCEEDED);
        }
    }
}
