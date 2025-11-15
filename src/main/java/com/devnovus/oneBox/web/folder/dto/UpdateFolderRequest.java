package com.devnovus.oneBox.web.folder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFolderRequest {
    private Long userId;         // 유저아이디
    private Long parentFolderId; // 상위폴더아이디

    @NotBlank(message = "폴더 이름은 비어 있을 수 없습니다.")
    @Size(max = 255, message = "폴더 이름은 최대 255자까지 허용됩니다.")
    @Pattern(regexp = "^[^\\\\/:*?\"<>|]+$", message = "폴더 이름에 허용되지 않는 특수문자가 포함되어 있습니다.")
    private String folderName;   // 폴더이름
}
