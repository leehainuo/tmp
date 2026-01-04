package com.miku.file.task;

import com.miku.file.config.FileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 清理临时文件任务（只处理 local 存储的 temp/ 目录）。
 * 需要在应用中启用 Scheduling（如：@EnableScheduling）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "miku.file.local", name = "enableTempCleanup", havingValue = "true")
public class TempFileCleanupTask {

    private final FileProperties fileProperties;

    /**
     * 每小时运行一次；如果你希望更细粒度可以在配置中关闭并自行启用。
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanup() {
        String basePath = fileProperties.getLocal().getPath();
        long retentionHours = fileProperties.getLocal().getTempRetentionHours();

        Path tempDir = Paths.get(basePath, "temp");
        if (!Files.exists(tempDir)) {
            return;
        }

        final Instant cutoff = Instant.now().minus(retentionHours, ChronoUnit.HOURS);

        try {
            Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Instant modified = attrs.lastModifiedTime().toInstant();
                    if (modified.isBefore(cutoff)) {
                        try {
                            Files.deleteIfExists(file);
                            log.info("Deleted temp file: {}", file);
                        } catch (Exception e) {
                            log.warn("Failed to delete temp file: {}", file, e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // try to delete empty directories
                    try {
                        Files.deleteIfExists(dir);
                    } catch (Exception ignored) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Failed to cleanup temp files under {}", tempDir, e);
        }
    }
}


