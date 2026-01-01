package com.miku.core.module.dept.dto;

import lombok.Data;

/**
 * 部门查询DTO
 *
 * @author lihainuo.com
 */
@Data
public class DeptQueryRequest {

    private Long current = 1L;

    private Long size = 10L;

    private String deptName;

    private Integer status;

    private Long parentId;

}
