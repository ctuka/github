package com.tkoseli.securecapita.resource;

import com.tkoseli.securecapita.domain.HttpResponse;
import com.tkoseli.securecapita.domain.User;
import com.tkoseli.securecapita.domain.UserPrincipal;
import com.tkoseli.securecapita.dto.UserDTO;
import com.tkoseli.securecapita.dto.dtomapper.UserDTOMapper;
import com.tkoseli.securecapita.form.LoginForm;
import com.tkoseli.securecapita.provider.TokenProvider;
import com.tkoseli.securecapita.service.RoleService;
import com.tkoseli.securecapita.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping(path = "/user")
@RequiredArgsConstructor
@Slf4j
public class UserResource {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;



    @PostMapping( "/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){
        log.info(loginForm.getPassword());
        authenticationManager.authenticate(unauthenticated(loginForm.getEmail(), loginForm.getPassword()));

        UserDTO user = userService.getUserByEmail(loginForm.getEmail());
        System.out.println(user.getEmail());
        return user.isUsingMfa() ? sendVerificationCode(user) : sendResponse(user);

    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO user) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                         "refresh_token" , tokenProvider.createRefreshToken(getUserPrincipal(user))))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private UserPrincipal getUserPrincipal(UserDTO user) {
        return new UserPrincipal(UserDTOMapper.toUser(userService.getUserByEmail(user.getEmail())), roleService.getRoleByUserId(user.getId()).getPermissions());
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO user) {
        userService.sendVerificationCode(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user))
                        .message("Verification Code Sent.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PostMapping( "/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid /* Any of the properties empty will throw exception
                                    */ User user){

        UserDTO userDto = userService.createUser(user);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto))
                        .message("UserCreated")
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build());

    }

    @GetMapping( "/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code ) {

        UserDTO user = userService.verifyCode(email, code);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                                "refresh_token" , tokenProvider.createRefreshToken(getUserPrincipal(user))))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());

    }

    @GetMapping( "/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication) {

        UserDTO user = userService.getUserByEmail(authentication.getName());
        System.out.println(authentication);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user))
                        .message("Profile Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());

    }

    @RequestMapping( "/error")
    public ResponseEntity<HttpResponse> handleError (HttpServletRequest request) {

        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("There is no mapping for a" + request.getMethod() + " request for this path on the server")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build());

    }

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());

    }

}
