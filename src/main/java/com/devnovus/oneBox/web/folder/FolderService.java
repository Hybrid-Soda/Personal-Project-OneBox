package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.*;
import com.devnovus.oneBox.web.folder.dto.CreateFolderRequest;
import com.devnovus.oneBox.web.folder.dto.DeleteFolderRequest;
import com.devnovus.oneBox.web.folder.dto.MetadataResponse;
import com.devnovus.oneBox.web.folder.dto.UpdateFolderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderMapper folderMapper;
    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;

    /** 폴더생성 */
    @Transactional
    public void createFolder(CreateFolderRequest req) {
        User user = findUser(req.getUserId());
        Metadata parentFolder = findFolder(req.getParentFolderId());

        // 검증
        validateFolderName(req.getFolderName(), req.getParentFolderId());
        validateChildFolderLimit(parentFolder.getId());
        validatePathLength(parentFolder.getPath(), req.getFolderName());

        metadataRepository.save(folderMapper.createMetadata(user, parentFolder, req.getFolderName()));
    }

    /** 폴더조회 */
    @Transactional(readOnly = true)
    public List<MetadataResponse> getListInFolder(Long folderId) {
        findFolder(folderId);

        return metadataRepository.findByParentFolderId(folderId)
                .stream()
                .map(folderMapper::createMetadataResponse)
                .toList();
    }

    /** 폴더수정 */
    @Transactional
    public void updateFolder(Long folderId, UpdateFolderRequest req) {
        Metadata folder = findFolder(folderId);
        Metadata parentFolder = findFolder(req.getParentFolderId());

        // 검증
        validateRootFolderUpdate(folder);
        validateRecursion(req.getParentFolderId(), folderId);
        validateFolderName(req.getFolderName(), req.getParentFolderId());
        validateChildFolderLimit(parentFolder.getId());
        validatePathLengthForUpdate(req.getUserId(), parentFolder.getPath(), folder.getPath(), folder.getName());

        // 이름과 상위 폴더 수정
        folder.setName(req.getFolderName());
        folder.setParentFolder(parentFolder);

        // 새 폴더 경로
        String newPrefix = parentFolder.getPath() + folder.getName() + "/";

        // 폴더와 하위 자원들의 경로 수정
        metadataRepository.updatePathByBulk(req.getUserId(), folder.getPath(), newPrefix);
    }

    /** 폴더삭제 */
    @Transactional
    public void deleteFolder(Long folderId, DeleteFolderRequest req) {
        Metadata folder = findFolder(folderId);
        metadataRepository.deleteAllChildren(req.getUserId(), folder.getPath());
    }

    // 유저 조회
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    // 폴더 조회 및 타입 검증
    private Metadata findFolder(Long folderId) {
        Metadata folder = metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));

        if (folder.getType() == MetadataType.FILE) throw new ApplicationException(ApplicationError.IS_NOT_FOLDER);

        return folder;
    }

    // 동일한 이름 존재 여부 검증
    private void validateFolderName(String folderName, Long parentFolderId) {
        Boolean isExist = metadataRepository.existsByNameAndParentFolderIdAndType(folderName, parentFolderId, MetadataType.FOLDER);

        if (isExist) throw new ApplicationException(ApplicationError.DUPLICATE_FOLDER_NAME);
    }

    // 하위 폴더 개수 제한 검증
    private void validateChildFolderLimit(Long parentFolderId) {
        long count = metadataRepository.countByParentFolderIdAndType(parentFolderId, MetadataType.FOLDER);

        if (count >= 100L) throw new ApplicationException(ApplicationError.TOO_MANY_CHILD_FOLDERS);
    }

    // 경로 길이 제한 검증 - 생성용
    private void validatePathLength(String newParentFolderPath, String folderName) {
        int newPathLength = newParentFolderPath.length() + folderName.length() + 1;

        if (newPathLength > 255) throw new ApplicationException(ApplicationError.PATH_LENGTH_EXCEEDED);
    }

    // 경로 길이 제한 검증 - 수정용
    private void validatePathLengthForUpdate(
            Long ownerId, String newParentFolderPath, String oldFolderPath, String folderName) {
        String longestChildPath = metadataRepository.findLongestChildPath(ownerId, oldFolderPath);
        String relative = longestChildPath.substring(oldFolderPath.length());
        int newPathLength = newParentFolderPath.length() + folderName.length() + 1 + relative.length();

        if (newPathLength > 255) throw new ApplicationException(ApplicationError.PATH_LENGTH_EXCEEDED);
    }

    // 자신의 하위 폴더 이동 검증
    private void validateRecursion(Long parentFolderId, Long nowFolderId) {
        if (parentFolderId.equals(nowFolderId)) {
            throw new ApplicationException(ApplicationError.MOVE_TO_ITSELF_NOT_ALLOWED);
        }
    }

    // 루트 폴더 이동 검증
    private void validateRootFolderUpdate(Metadata folder) {
        if (folder.getParentFolder() == null) {
            throw new ApplicationException(ApplicationError.ROOT_FOLDER_UPDATE_NOT_ALLOWED);
        }
    }
}
