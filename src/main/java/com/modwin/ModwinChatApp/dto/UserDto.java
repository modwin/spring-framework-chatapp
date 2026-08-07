package com.modwin.ModwinChatApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modwin.ModwinChatApp.persistence.model.Role;
import com.modwin.ModwinChatApp.persistence.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {

    @NotBlank(message = "Email field is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NonNull
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    private String username;

    @NonNull
    @NotBlank(message = "Name field is required.")
    private String name;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @NotBlank(message = "Password field is required.")
    @Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Builder.Default
    private List<UserDto> friends = new ArrayList<>();

    public UserDto(@NonNull String email, @NonNull String username, @NonNull String name, String password) {
        this.email = email;
        this.username = username;
        this.name = name;
        this.password = password;
    }

}
