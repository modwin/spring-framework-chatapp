package com.modwin.ModwinChatApp.persistence.repository;

import com.modwin.ModwinChatApp.persistence.model.Friendship;
import com.modwin.ModwinChatApp.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    @Query("""
            select (count(f) > 0) from Friendship f
            where (f.requester = :first and f.recipient = :second)
               or (f.requester = :second and f.recipient = :first)
            """)
    boolean existsBetween(@Param("first") User first, @Param("second") User second);

    @EntityGraph(attributePaths = {"requester", "recipient"})
    @Query("""
            select f from Friendship f
            where f.requester.id = :userId or f.recipient.id = :userId
            order by f.id desc
            """)
    List<Friendship> findAllForUser(@Param("userId") Integer userId);
}
