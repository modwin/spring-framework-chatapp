package com.modwin.ModwinChatApp.persistence.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity(name="ROLES")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="role_id",unique = true, nullable = false)
    private Integer ID;

    @NonNull
    @Column(unique = true, nullable = false)
    private String name;

    public Role() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(ID, role.ID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }
}
