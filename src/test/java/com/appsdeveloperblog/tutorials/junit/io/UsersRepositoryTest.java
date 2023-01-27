package com.appsdeveloperblog.tutorials.junit.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UsersRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UsersRepository usersRepository;

    private UserEntity user;

    @BeforeEach
    void setup() {
        user = new UserEntity();
        user.setUserId(UUID.randomUUID().toString());
        user.setFirstName("Bruno");
        user.setLastName("Barbosa");
        user.setEmail("email@email.com");
        user.setEncryptedPassword("12345678");

        testEntityManager.persistAndFlush(user);

        UserEntity user2 = new UserEntity();
        user2.setUserId(UUID.randomUUID().toString());
        user2.setFirstName("Cassia");
        user2.setLastName("Cunha");
        user2.setEmail("email2@email.com");
        user2.setEncryptedPassword("12345678");

        testEntityManager.persistAndFlush(user2);
    }

    @Test
    void testFindByEmail_whenGivenCorrectEmail_returnsUserEntity() {

        //Act
        UserEntity storedUser = usersRepository.findByEmail(user.getEmail());

        //Assert
        assertEquals(user.getEmail(), storedUser.getEmail(), "The returned email address does not match the expected value");
    }

    @Test
    void testFindByUserId_whenGivenCorrectUserId_returnsUserEntity() {
        //Act
        UserEntity storedUser = usersRepository.findByUserId(user.getUserId());

        //Assert
        assertNotNull(storedUser, "User entity should not be null");
        assertEquals(user.getUserId(), storedUser.getUserId(), "The user id is not the same as expected");
    }

    @Test
    void testFindUsersWithEmailEndingWith_whenGiveEmailDomain_returnsUserWithGivenDomain() {
        //Arrange
        UserEntity user = new UserEntity();
        user.setFirstName("Google");
        user.setLastName("Oogle");
        user.setEmail("test@gmail.com");
        user.setEncryptedPassword("12345678");
        user.setUserId(UUID.randomUUID().toString());

        testEntityManager.persistAndFlush(user);
        String emailDomainName = "@gmail.com";

        //Act
        List<UserEntity> users = usersRepository.findUsersWithEmailEndingWith(emailDomainName);

        //Assert
        assertEquals(1, users.size(), "There should be only one user in the list");
        assertTrue(users.get(0).getEmail().endsWith(emailDomainName));
    }

}
