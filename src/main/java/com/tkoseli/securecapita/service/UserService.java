package com.tkoseli.securecapita.service;

import com.tkoseli.securecapita.domain.User;
import com.tkoseli.securecapita.dto.UserDTO;

public interface UserService {
    UserDTO createUser(User user);

    UserDTO getUserByEmail(String email);

    void sendVerificationCode(UserDTO user);



    UserDTO verifyCode(String email, String code);
}
