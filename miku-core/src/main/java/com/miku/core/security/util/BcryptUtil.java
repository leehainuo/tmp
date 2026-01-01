package com.miku.core.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Scanner;

/**
 * BCrypt密码加密工具类
 */
public class BcryptUtil {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        System.out.println("请输入要加密的密码");
        Scanner scanner = new Scanner(System.in);
        String password = scanner.next();
        String hash = encode(password);
        System.out.println("加密后的hash: " + hash);
        System.out.println("验证结果: " + matches(password, hash));
    }
}

