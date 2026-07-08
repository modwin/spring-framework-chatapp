package com.modwin.ModwinChatApp.persistence.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_id",unique = true, nullable = false)
    private Integer ID;

    private Set<User> users;

    @OneToMany
    private List<Message> messages;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
