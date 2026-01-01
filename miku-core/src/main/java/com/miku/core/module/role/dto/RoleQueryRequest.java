package com.miku.core.module.role.dto;

import lombok.Data;

/**
 * 角色查询DTO
 */
@Data
public class RoleQueryRequest {

    private Long current = 1L;

    private Long size = 10L;

    private String roleName;

    private String roleCode;

    private Integer status;

}