package org.ithub.userservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.ithub.userservice.dto.UserDto;
import org.ithub.userservice.enums.Role;
import org.ithub.userservice.model.User;
import org.ithub.userservice.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        log.info("Get current user");
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    @Deprecated
    public UserDto getAdmin() {
        var user = getCurrentUser();
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new RuntimeException("User already with the ADMIN role");
        }

        log.info("Current user is change role to ADMIN");
        user.setRole(Role.ROLE_ADMIN);
        return createUser(user);
    }

//    @Transactional
    public UserDto createUser(User user) {
        log.info("Save user '{}' with id {}", user.getUsername(), user.getId());
        return mapToDto(userRepository.save(user));
    }

    public UserDto getUserById(Long id) {
        log.info("Get user with id {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Users not found"));
        return mapToDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.info("Get all users");
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Update user '{}' with id {}", userDto.getUsername(), userDto.getId());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Users not found"));
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    public User getByUsername(String username) {
        log.info("Get user with username {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Users not found"));
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Delete user with id {}", id);
        userRepository.deleteById(id);
    }

    public UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getUsername(), user.getRole());
    }
}
