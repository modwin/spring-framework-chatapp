package com.modwin.ModwinChatApp.persistence.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.*;

@Getter
@Setter
@Entity(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {

    @NonNull
    @Column(unique = true, nullable = false)
    private String email;
    @NonNull
    @Column(unique = true, nullable = false)
    private String username;
    @NonNull
    @Column(nullable = false)
    private String name;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Nullable
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id",unique = true, nullable = false)
    private Integer id;


    // TODO: Add Friendship class to be able to add pending friends requests, blocked status etc.
/*    @Builder.Default
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "users_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();*/

    public User(@NonNull String email, @NonNull String username, @NonNull String name, String password) {
        this.email = email;
        this.username = username;
        this.name = name;
        this.password = password;
        this.roles = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && user.id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
