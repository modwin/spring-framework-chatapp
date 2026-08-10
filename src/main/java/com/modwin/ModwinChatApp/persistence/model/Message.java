package com.modwin.ModwinChatApp.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    private LocalDateTime lastEdit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User sender;

    @PrePersist
    void setInitialTimestamp() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
