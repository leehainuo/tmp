package com.miku.core.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT Token对
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {
    
    /**
     * 访问Token
     */
    private String accessToken;
    
    /**
     * 刷新Token
     */
    private String refreshToken;
    
    /**
     * 访问Token过期时间（秒）
     */
    private Long accessTokenExpiresIn;
    
    /**
     * 刷新Token过期时间（秒）
     */
    private Long refreshTokenExpiresIn;
}

