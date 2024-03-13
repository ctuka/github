package com.tkoseli.securecapita.service.implementation;

import com.tkoseli.securecapita.domain.Role;
import com.tkoseli.securecapita.repository.RoleRepository;
import com.tkoseli.securecapita.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository<Role> roleRepository;

    @Override
    public Role getRoleByUserId(Long id) {
        return roleRepository.getRoleByUserId(id);
    }
}
