package com.miku.file.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 *
 * @author miku
 * @date 2025-11-03
 */
public class FileUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    
    /**
     * 生成文件名（UUID）
     *
     * @param originalFileName 原始文件名
     * @return 新文件名
     */
    public static String generateFileName(String originalFileName) {
        String extension = getExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        if (extension != null && !extension.isEmpty()) {
            return uuid + "." + extension;
        }
        return uuid;
    }
    
    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            return fileName.substring(index + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 获取按日期分组的路径
     *
     * @return 日期路径，如：2025/11/03
     */
    public static String getDatePath() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
    
    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的大小，如：1.23 MB
     */
    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 验证文件扩展名
     *
     * @param fileName 文件名
     * @param allowedExtensions 允许的扩展名
     * @return 是否允许
     */
    public static boolean isAllowedExtension(String fileName, String... allowedExtensions) {
        String extension = getExtension(fileName);
        
        for (String allowed : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }
}

