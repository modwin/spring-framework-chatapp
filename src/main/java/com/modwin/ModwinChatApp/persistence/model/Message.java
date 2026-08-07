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

    @ManyToAny(fetch = FetchType.LAZY)
    private String content;

    private LocalDateTime sentAt;
    private LocalDateTime lastEdit;

    @Getter
    @Setter
    @OneToOne
    @JoinColumn(name = "sender_user_id")
    private User sender;


}
