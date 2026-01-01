package com.miku.core.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 *
 * @author lihainuo.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    /**
     * 访问令牌过期时间（秒）
     */
    @Schema(description = "访问令牌过期时间（秒）")
    private Long accessTokenExpiresIn;

    /**
     * 刷新令牌过期时间（秒）
     */
    @Schema(description = "刷新令牌过期时间（秒）")
    private Long refreshTokenExpiresIn;

    public LoginResponse(String accessToken, String refreshToken, Long accessTokenExpiresIn, Long refreshTokenExpiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

}

