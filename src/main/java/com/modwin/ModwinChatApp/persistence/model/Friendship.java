package com.modwin.ModwinChatApp.persistence.model;

import com.modwin.ModwinChatApp.util.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "friendship")
@Check(constraints = "requester_id <> recipient_id")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status;

    public Friendship(User requester, User recipient, FriendshipStatus status) {
        this.requester = requester;
        this.recipient = recipient;
        this.status = status;
    }
}
