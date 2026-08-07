package com.modwin.ModwinChatApp.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ManyToAny;

import java.time.LocalDateTime;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id",unique = true, nullable = false)
    private Integer ID;

    @ManyToOne
    private Chat chat;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column
    private LocalDateTime sentAt;
    @Column
    private LocalDateTime lastEdit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User sender;


}
