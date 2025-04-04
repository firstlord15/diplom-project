package org.ithub.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ithub.userservice.dto.JwtAuthenticationResponse;
import org.ithub.userservice.dto.SignInRequest;
import org.ithub.userservice.dto.SignUpRequest;
import org.ithub.userservice.enums.Role;
import org.ithub.userservice.service.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up/user")
    public JwtAuthenticationResponse signUpUser(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request, Role.USER);
    }

    @Operation(summary = "Регистрация админа")
    @PostMapping("/sign-up/admin")
    public JwtAuthenticationResponse signUpAdmin(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request, Role.ADMIN);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }
}
