package com.tkoseli.securecapita.service.implementation;

import com.tkoseli.securecapita.domain.Role;
import com.tkoseli.securecapita.domain.User;
import com.tkoseli.securecapita.dto.UserDTO;
import com.tkoseli.securecapita.dto.dtomapper.UserDTOMapper;
import com.tkoseli.securecapita.repository.RoleRepository;
import com.tkoseli.securecapita.repository.UserRepository;
import com.tkoseli.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRoleRepository;
    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepository.create(user));
    }


    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepository.getUserByEmail(email));
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        userRepository.sendVerificationCode(user);
    }


    @Override
    public UserDTO verifyCode(String email, String code) {
        return UserDTOMapper.fromUser(userRepository.verifyCode(email, code));
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTOMapper.fromUser(user, roleRoleRepository.getRoleByUserId(user.getId()));
    }
}
