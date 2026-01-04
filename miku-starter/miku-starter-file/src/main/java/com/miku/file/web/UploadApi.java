package com.miku.file.web;

import com.miku.file.common.Result;
import com.miku.file.entity.FileInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Upload API contract. Applications can provide their own @RestController bean
 * implementing this interface to override the starter's default controller.
 */
public interface UploadApi {

    @PostMapping
    Result<FileInfo> upload(MultipartFile file) throws IOException;

    @GetMapping("/download/{path}")
    void download(@PathVariable String path, HttpServletResponse response) throws IOException;
}


