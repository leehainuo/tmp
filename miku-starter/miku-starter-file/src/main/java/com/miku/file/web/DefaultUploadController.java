package com.miku.file.web;

import com.miku.file.common.Result;
import com.miku.file.entity.FileInfo;
import com.miku.file.service.FileStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import com.miku.file.util.FileUtils;
import com.miku.file.config.FileProperties;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.time.Duration;

/**
 * Default controller provided by starter. It is only created if no other bean
 * implementing UploadApi is present. Consumers can implement UploadApi in their
 * application to override this controller.
 */
@Slf4j
@Tag(name = "文件存储 - 默认控制器")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@ConditionalOnMissingBean(UploadApi.class)
@ConditionalOnProperty(name = "miku.file.expose-controller", havingValue = "true", matchIfMissing = true)
public class DefaultUploadController implements UploadApi {

    private final FileStorage fileStorage;
    private final FileProperties fileProperties;

    @Operation(summary = "通用文件上传（默认实现）")
    @PostMapping
    public Result<FileInfo> upload(@RequestParam("file") MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        // store under temp/... by default to support finalize flow
        String path = "temp/" + FileUtils.getDatePath() + "/" + FileUtils.generateFileName(file.getOriginalFilename());
        FileInfo fileInfo = fileStorage.upload(inputStream, path, file.getOriginalFilename(), file.getContentType());
        // Always generate absolute URL from current request context
        String relative = fileInfo.getUrl(); // prefix + "/" + path
        String abs = ServletUriComponentsBuilder.fromCurrentContextPath().path(relative).toUriString();
        fileInfo.setUrl(abs);
        return Result.success(fileInfo);
    }

    @Operation(summary = "下载文件（默认实现）")
    @GetMapping("/download/{path}")
    public void download(@PathVariable String path, HttpServletResponse response) throws IOException {
        InputStream inputStream = fileStorage.download(path);
        try {
            inputStream.transferTo(response.getOutputStream());
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Operation(summary = "Finalize a temporary upload and move into permanent storage")
    @PostMapping("/finalize")
    public Result<FileInfo> finalizeUpload(@RequestBody FinalizeRequest req) throws IOException {
        String tempPath = req.getTempPath();
        if (tempPath == null || tempPath.isEmpty()) {
            return Result.error("tempPath required");
        }

        // retrieve original metadata
        FileInfo tempInfo = fileStorage.getFileInfo(tempPath);
        InputStream is = fileStorage.download(tempPath);

        String targetDir = req.getTargetDir() != null ? req.getTargetDir() : "";
        String targetPath = (targetDir.isEmpty() ? "" : (targetDir + "/")) + FileUtils.getDatePath() + "/" + FileUtils.generateFileName(tempInfo.getOriginalFileName());

        FileInfo finalInfo = fileStorage.upload(is, targetPath, tempInfo.getOriginalFileName(), tempInfo.getContentType());

        // remove temp
        try {
            fileStorage.delete(tempPath);
        } catch (Exception e) {
            log.warn("failed to delete temp file: {}", tempPath, e);
        }
        // Always generate absolute URL from current request context
        String relative = finalInfo.getUrl();
        String abs = ServletUriComponentsBuilder.fromCurrentContextPath().path(relative).toUriString();
        finalInfo.setUrl(abs);
        return Result.success(finalInfo);
    }
}


