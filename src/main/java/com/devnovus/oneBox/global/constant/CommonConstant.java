package com.devnovus.oneBox.global.constant;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CommonConstant {
    public static final Pattern INVALID_NAME_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");
    public static final Set<String> BLOCKED_MIME_TYPES = Set.of("application/x-msdownload", "application/x-sh");
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static final Map<String, String> MIME_TYPES = Map.ofEntries(
            // 애플리케이션
            Map.entry("json", "application/json"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("zip", "application/zip"),
            Map.entry("gz", "application/gzip"),
            Map.entry("xml", "application/xml"),
            Map.entry("sh", "application/x-sh"),
            Map.entry("tar", "application/x-tar"),
            Map.entry("exe", "application/x-msdownload"),
            // 오디오
            Map.entry("mp3", "audio/mpeg"),
            Map.entry("aac", "audio/aac"),
            Map.entry("ogg", "audio/ogg"),
            // 이미지
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("bmp", "image/bmp"),
            // 텍스트
            Map.entry("txt", "text/plain"),
            Map.entry("html", "text/html"),
            Map.entry("css", "text/css"),
            Map.entry("csv", "text/csv"),
            // 비디오
            Map.entry("mp4", "video/mp4"),
            Map.entry("avi", "video/x-msvideo")
    );

    public static final int MAX_CHILD_FOLDERS = 100;
    public static final int MAX_FILE_NAME_LENGTH = 254;
}
