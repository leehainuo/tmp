package com.miku.core.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求
 *
 * @author lihainuo.com
 */
@Data
@Schema(description = "刷新Token请求")
public class RefreshTokenRequest {

    @NotBlank(message = "刷新Token不能为空")
    @Schema(description = "刷新Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}
