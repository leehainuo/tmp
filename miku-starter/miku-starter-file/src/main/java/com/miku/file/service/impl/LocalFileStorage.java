package com.miku.file.service.impl;

import com.miku.file.config.FileProperties;
import com.miku.file.entity.FileInfo;
import com.miku.file.service.FileStorage;
import com.miku.file.util.ContentHashUtil;
import com.miku.file.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储实现
 *
 * @author miku
 * @date 2025-11-03
 */
@Slf4j
@RequiredArgsConstructor
public class LocalFileStorage implements FileStorage {

    private final FileProperties.LocalConfig config;

    @Override
    public FileInfo upload(InputStream inputStream, String originalFileName, String contentType) {
        String fileName = FileUtils.generateFileName(originalFileName);
        String path = FileUtils.getDatePath() + "/" + fileName;
        return upload(inputStream, path, originalFileName, contentType);
    }

    @Override
    public FileInfo upload(InputStream inputStream, String path, String originalFileName, String contentType) {
        try {
            // 构建完整路径
            Path fullPath = Paths.get(config.getPath(), path);

            // 创建目录
            Files.createDirectories(fullPath.getParent());

            // 保存文件
            Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);

            // 获取文件大小
            long size = Files.size(fullPath);

            // 构建文件信息
            return FileInfo.builder()
                    .fileName(fullPath.getFileName().toString())
                    .originalFileName(originalFileName)
                    .extension(FileUtils.getExtension(originalFileName))
                    .size(size)
                    .contentType(contentType)
                    .path(path)
                    .url(getUrl(path))
                    .storageType("local")
                    .uploadTime(System.currentTimeMillis())
                    .build();

        } catch (IOException e) {
            log.error("本地文件上传失败: {}", path, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public FileInfo uploadWithHash(InputStream inputStream, String targetDir, String originalFileName, String contentType) {
        try {
            // write stream to temp file while computing hash
            Path tempDir = Paths.get(config.getPath(), "temp", FileUtils.getDatePath());
            Files.createDirectories(tempDir);
            Path tempFile = Files.createTempFile(tempDir, "up-", ".tmp");

            String hash = ContentHashUtil.computeHashAndWriteTemp(inputStream, tempFile);
            String ext = FileUtils.getExtension(originalFileName);
            String fileName = (ext != null && !ext.isEmpty()) ? (hash + "." + ext) : hash;

            String finalPath = (targetDir == null || targetDir.isEmpty()) ? fileName : (targetDir + "/" + fileName);

            Path fullPath = Paths.get(config.getPath(), finalPath);
            // if file already exists, delete temp and return existing info
            if (Files.exists(fullPath)) {
                Files.deleteIfExists(tempFile);
                return getFileInfo(finalPath);
            }

            // ensure parent dirs, move temp -> final
            Files.createDirectories(fullPath.getParent());
            Files.move(tempFile, fullPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            long size = Files.size(fullPath);
            return FileInfo.builder()
                    .fileName(fullPath.getFileName().toString())
                    .originalFileName(originalFileName)
                    .extension(ext)
                    .size(size)
                    .contentType(contentType)
                    .path(finalPath)
                    .url(getUrl(finalPath))
                    .storageType("local")
                    .uploadTime(System.currentTimeMillis())
                    .build();
        } catch (IOException e) {
            log.error("本地文件上传失败 (hash): {}", originalFileName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            Path fullPath = Paths.get(config.getPath(), path);
            return Files.newInputStream(fullPath);
        } catch (IOException e) {
            log.error("本地文件下载失败: {}", path, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public boolean delete(String path) {
        try {
            Path fullPath = Paths.get(config.getPath(), path);
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            log.error("本地文件删除失败: {}", path, e);
            return false;
        }
    }

    @Override
    public boolean exists(String path) {
        Path fullPath = Paths.get(config.getPath(), path);
        return Files.exists(fullPath);
    }

    @Override
    public String getUrl(String path) {
        if (config.getEnableDirectAccess()) {
            // derive prefix from storage directory name
            String name = Path.of(config.getPath()).getFileName().toString();
            String prefix = name.startsWith("/") ? name : "/" + name;
            String relative = prefix.endsWith("/") ? prefix + path : prefix + "/" + path;
            return relative;
        }
        return path;
    }

    @Override
    public FileInfo getFileInfo(String path) {
        try {
            Path fullPath = Paths.get(config.getPath(), path);

            if (!Files.exists(fullPath)) {
                return null;
            }

            return FileInfo.builder()
                    .fileName(fullPath.getFileName().toString())
                    .extension(FileUtils.getExtension(path))
                    .size(Files.size(fullPath))
                    .path(path)
                    .url(getUrl(path))
                    .storageType("local")
                    .build();

        } catch (IOException e) {
            log.error("获取本地文件信息失败: {}", path, e);
            return null;
        }
    }
}

