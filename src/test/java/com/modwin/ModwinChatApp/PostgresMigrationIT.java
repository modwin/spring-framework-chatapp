package com.modwin.ModwinChatApp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class PostgresMigrationIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE message, chat_users, chat, friendship, user_roles, roles, users
                RESTART IDENTITY CASCADE
                """);
    }

    @Test
    void migrationMatchesTheJpaModel() {
        Integer migrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success",
                Integer.class
        );

        assertThat(migrationCount).isEqualTo(2);
    }

    @Test
    void reciprocalFriendRequestsCannotBothCommit() throws Exception {
        int aliceId = insertUser("alice", "alice@example.com");
        int bobId = insertUser("bob", "bob@example.com");
        CyclicBarrier startTogether = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> aliceToBob = executor.submit(
                    () -> insertFriendship(startTogether, aliceId, bobId)
            );
            Future<Boolean> bobToAlice = executor.submit(
                    () -> insertFriendship(startTogether, bobId, aliceId)
            );

            assertThat(List.of(
                    aliceToBob.get(10, TimeUnit.SECONDS),
                    bobToAlice.get(10, TimeUnit.SECONDS)
            )).containsExactlyInAnyOrder(true, false);
        } finally {
            executor.shutdownNow();
        }

        Integer friendshipCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendship",
                Integer.class
        );
        assertThat(friendshipCount).isEqualTo(1);
    }

    private int insertUser(String username, String email) {
        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO users (email, username, name, password)
                        VALUES (?, ?, ?, ?)
                        RETURNING user_id
                        """,
                Integer.class,
                email,
                username,
                username,
                "encoded-password"
        );
    }

    private boolean insertFriendship(CyclicBarrier startTogether, int requesterId, int recipientId)
            throws Exception {
        startTogether.await(5, TimeUnit.SECONDS);
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    jdbcTemplate.update(
                            """
                                    INSERT INTO friendship (requester_id, recipient_id, status)
                                    VALUES (?, ?, 'PENDING')
                                    """,
                            requesterId,
                            recipientId
                    )
            );
            return true;
        } catch (DataIntegrityViolationException constraintViolation) {
            return false;
        }
    }
}
