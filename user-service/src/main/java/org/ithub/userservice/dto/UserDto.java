package org.ithub.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.userservice.enums.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private long id;
    private String email;
    private String username;
    private Role role;

    public UserDto(String email, String username, Role role) {
        this.email = email;
        this.username = username;
        this.role = role;
    }

    public UserDto(String email, String username) {
        this.email = email;
        this.username = username;
    }
}
