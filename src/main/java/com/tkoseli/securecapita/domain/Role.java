package com.tkoseli.securecapita.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * @author Tevfik Koseli
 * @version 1.0
 * @since 12/15/2023
 */

@Data
@SuperBuilder //together with this we dont use setter getter while using RoleRowMapper
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Role {
    public Long id;
    @NotEmpty(message =  "First name cannot be empty")
    public String name;
    @NotEmpty(message =  "Permission cannot be empty")
    public String permissions;

}
