package com.miku.core.module.user.dto;

import lombok.Data;

@Data
public class UserQueryRequest {

    private Long current = 1L;

    private Long size = 10L;

    private String username;

    private String phone;

    private Integer status;

}
