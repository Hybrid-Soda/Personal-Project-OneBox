package com.devnovus.oneBox.domain.folder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoveFolderRequest {
    private Long userId;         // 유저아이디
    private Long parentFolderId; // 상위폴더아이디
}
