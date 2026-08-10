package com.modwin.ModwinChatApp.persistence.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @NonNull
    @Column(unique = true, nullable = false, length = 254)
    private String email;

    @NonNull
    @Column(unique = true, nullable = false, length = 20)
    private String username;

    @NonNull
    @Column(nullable = false, length = 100)
    private String name;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(length = 100)
    private String password;

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
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && user.id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
