package com.modwin.ModwinChatApp.persistence.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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
    private Integer id;

    @ManyToMany
    private Set<User> users;

    @OneToMany(mappedBy = "chat")
    private List<Message> messages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
