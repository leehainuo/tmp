package com.miku.core.module.role.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限ID列表类型处理器
 * 用于处理 GROUP_CONCAT 返回的字符串转换为 List<Long>
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class PermissionIdsTypeHandler extends BaseTypeHandler<List<Long>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setString(i, null);
        } else {
            String value = parameter.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            ps.setString(i, value);
        }
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseStringToList(value);
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseStringToList(value);
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseStringToList(value);
    }

    private List<Long> parseStringToList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .filter(s -> !s.trim().isEmpty())
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}

