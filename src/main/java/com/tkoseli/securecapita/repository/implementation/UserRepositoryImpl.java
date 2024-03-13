package com.tkoseli.securecapita.repository.implementation;

import com.tkoseli.securecapita.domain.Role;
import com.tkoseli.securecapita.domain.User;
import com.tkoseli.securecapita.domain.UserPrincipal;
import com.tkoseli.securecapita.dto.UserDTO;
import com.tkoseli.securecapita.exception.ApiException;
import com.tkoseli.securecapita.repository.RoleRepository;
import com.tkoseli.securecapita.repository.UserRepository;
import com.tkoseli.securecapita.rowmapper.UserRowMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.tkoseli.securecapita.enumeration.RoleType.ROLE_USER;
import static com.tkoseli.securecapita.enumeration.VerificationType.ACCOUNT;
import static com.tkoseli.securecapita.query.UserQuery.*;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Repository
@RequiredArgsConstructor
@Slf4j

public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {


    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public User create(User user) {
        //Check the email is uniqe
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0) throw new ApiException("Email already in use.Please use different email");
        //Save the new user
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getSqlParametersSource(user);
            jdbc.update(INSER_USER_QUERY, parameters, holder);
            user.setId(requireNonNull(holder.getKey()).longValue());
            //Add role to user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            // Send verification Url
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            //Save Url in verification Table
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));
            //Send email to user with verification URL
            //emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);
            //Return newly created
            return user;
            //If any errors, throw exception with proper message
        }

        catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No Role found by name:" + ROLE_USER.name());
        }
        catch (Exception exeption) {
            log.error(exeption.getMessage());
            throw new ApiException("An Error occured.Please try again");
        }

    }

    @Override
    public Collection list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    private Integer getEmailCount( String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }

    private SqlParameterSource getSqlParametersSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if (user == null) {
            log.error("User not found in the database.");
            throw new UsernameNotFoundException("User not found in the database.");
        }
        else {
            log.info("User  found in the database: {}", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()).getPermissions());
        }

    }

    @Override
    public User getUserByEmail(String email) {
        try {

            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
            return user;
        }

        catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No User found by email:" + email);
        }
        catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An Error occured.Please try again");
        }
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT );
        String verificationCode = randomAlphabetic(8).toUpperCase();

        try {
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID, Map.of("id", user.getId()));

            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, Map.of("userId", user.getId(), "code", verificationCode, "expirationDate", expirationDate));
            //sendSMS(user.getPhone(), "From: securecapita \nVerification code\n" + verificationCode);
            log.info("Verification Code : {}", verificationCode);

        }

        catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An Error occured.Please try again");
        }
    }

    @Override
    public User verifyCode(String email, String code) {
        if(isVerificationCodeExpired(code)) throw new ApiException("This code is expired.Please login again.");

        try {
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY, Map.of("code", code), new
                    UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
            if (userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                jdbc.update(DELETE_CODE, Map.of("code", code)); //Help use verification code once
                return userByCode;
            } else  {
                throw new ApiException("Code is invalid.Please try again");
            }

        } catch (EmptyResultDataAccessException exception) {

            throw new ApiException("Could not find Record");
        } catch (Exception exception) {
            throw new ApiException("An error occured please try again.");
        }
    }

    private Boolean isVerificationCodeExpired(String code) {
        try {
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, Map.of("code", code), Boolean.class);


        } catch (EmptyResultDataAccessException exception) {

            throw new ApiException("This code is not valid.Please login again.");
        } catch (Exception exception) {
            throw new ApiException("An error occured please try again.");
        }
    }


}
