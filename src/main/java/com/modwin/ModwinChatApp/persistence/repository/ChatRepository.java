package com.modwin.ModwinChatApp.persistence.repository;

import com.modwin.ModwinChatApp.persistence.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Override
    Optional<Chat> findById(Integer integer);
}
