package org.ithub.userservice.service;

import lombok.RequiredArgsConstructor;
import org.ithub.userservice.dto.JwtAuthenticationResponse;
import org.ithub.userservice.dto.SignInRequest;
import org.ithub.userservice.dto.SignUpRequest;
import org.ithub.userservice.enums.Role;
import org.ithub.userservice.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse signUp(SignUpRequest request, Role role) {
        User user = new User(
                request.getEmail(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role
        );

        userService.createUser(user);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
