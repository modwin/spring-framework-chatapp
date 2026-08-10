package com.modwin.ModwinChatApp.persistence.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    public Role(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Role role = (Role) o;
        return id != null && role.id != null && Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
