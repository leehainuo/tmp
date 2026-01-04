package com.miku.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置
 *
 * @author miku
 * @date 2025-11-03
 */
@Data
@ConfigurationProperties(prefix = "miku.file")
public class FileProperties {
    
    /**
     * 存储类型：local(本地), oss(阿里云), minio
     */
    private StorageType type = StorageType.LOCAL;
    
    /**
     * 本地存储配置
     */
    private LocalConfig local = new LocalConfig();
    
    /**
     * 阿里云 OSS 配置
     */
    private OssConfig oss = new OssConfig();
    
    /**
     * MinIO 配置
     */
    private MinioConfig minio = new MinioConfig();
    
    /**
     * 存储类型枚举
     */
    public enum StorageType {
        LOCAL, OSS, MINIO
    }
    
    /**
     * 本地存储配置
     */
    @Data
    public static class LocalConfig {
        /**
         * 本地存储路径
         */
        private String path = "./file";
        
        /**
         * 是否启用直接访问（通过 HTTP）
         */
        private Boolean enableDirectAccess = true;
        
        /**
         * 临时文件保留时长（小时），默认 24 小时
         */
        private Long tempRetentionHours = 24L;
        
        /**
         * 是否启用临时文件清理任务（需要应用启用 Scheduling）
         */
        private Boolean enableTempCleanup = false;
    }
    
    /**
     * 阿里云 OSS 配置
     */
    @Data
    public static class OssConfig {
        /**
         * 访问密钥 ID
         */
        private String accessKeyId;
        
        /**
         * 访问密钥密码
         */
        private String accessKeySecret;
        
        /**
         * Endpoint
         */
        private String endpoint;
        
        /**
         * Bucket 名称
         */
        private String bucketName;
        
        /**
         * 自定义域名（可选）
         */
        private String customDomain;
        
        /**
         * 文件前缀路径
         */
        private String prefix = "miku/";
    }
    
    /**
     * MinIO 配置
     */
    @Data
    public static class MinioConfig {
        /**
         * MinIO 服务地址
         */
        private String endpoint;
        
        /**
         * 访问密钥
         */
        private String accessKey;
        
        /**
         * 访问密钥密码
         */
        private String secretKey;
        
        /**
         * Bucket 名称
         */
        private String bucketName;
        
        /**
         * 文件前缀路径
         */
        private String prefix = "miku/";
    }
}

