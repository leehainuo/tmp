package com.miku.file.web;

import lombok.Data;

/**
 * Request for finalizing a temporary upload.
 */
@Data
public class FinalizeRequest {
    /**
     * 临时路径（由 /upload 返回的 FileInfo.path）
     */
    private String tempPath;
    /**
     * 可选目标目录（例如 avatars），不需要包含日期
     */
    private String targetDir;
}


