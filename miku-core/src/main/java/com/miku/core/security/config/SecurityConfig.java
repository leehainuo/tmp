package com.miku.core.security.config;

import com.miku.core.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // 默认 prePostEnabled = true
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 手动配置认证提供者（避免警告）
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        http
                // 禁用CSRF（使用JWT不需要）
                .csrf(AbstractHttpConfigurer::disable)

                // 禁用Session（使用JWT不需要）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 允许匿名访问的路径
                        .requestMatchers(
                                "/auth/login",
                                "/auth/logout",
                                "/auth/refresh",
                                "/health",
                                "/doc.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/file/**",
                                "/favicon.ico"
                        ).permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )

                // 配置认证提供者
                .authenticationProvider(authProvider)

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 配置异常处理
                .exceptionHandling(exception -> exception
                        // 未认证处理
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"code\":401,\"message\":\"未认证，请先登录\"}");
                        })
                        // 无权限处理
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
                        })
                );

        return http.build();
    }
}

