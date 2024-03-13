package com.tkoseli.securecapita.service;

import com.tkoseli.securecapita.domain.Role;

public interface RoleService {
    Role getRoleByUserId(Long id);
}
