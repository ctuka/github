package com.tkoseli.securecapita.rowmapper;

import com.tkoseli.securecapita.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;



public class RoleRowMapper implements RowMapper<Role> { // Database Rows to java Object

    @Override
    public Role mapRow(ResultSet resultSet, int rowNum) throws SQLException { //@SuperBuilder annotaion helped us using this like that intead of using setter getter
        return Role.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .permissions(resultSet.getString("permissions"))
                .build();
}

}