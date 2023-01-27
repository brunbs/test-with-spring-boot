package com.appsdeveloperblog.tutorials.junit.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserEntityIntegrationTest {

    @Autowired
    private TestEntityManager testEntityManager;

    private UserEntity userEntity;

    @BeforeEach
    void setup() {
        userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setFirstName("Bruno");
        userEntity.setLastName("Barbosa");
        userEntity.setEmail("email@email.com");
        userEntity.setEncryptedPassword("12345678");
    }

    @Test
    void testUserEntity_whenValidUserDetailsProvidade_shouldReturnStoredUserDetails() {
        //Arrange

        //Act
        UserEntity storedUserEntity = testEntityManager.persistAndFlush(userEntity);

        //Assert
        assertTrue(storedUserEntity.getId() > 0);
        assertEquals(userEntity.getFirstName(), storedUserEntity.getFirstName(), "First name should be the same");
        assertEquals(userEntity.getLastName(), storedUserEntity.getLastName(), "Last name should be the same");
        assertEquals(userEntity.getEmail(), storedUserEntity.getEmail(), "Email name should be the same");
        assertEquals(userEntity.getEncryptedPassword(), storedUserEntity.getEncryptedPassword(), "Password name should be the same");
    }

    @Test
    void testUserEntity_whenFirstNameIsTooLong_shouldThrowExceptio() {
        //Arrange
        userEntity.setFirstName("23123131312312?23131312312?23131312312?23131312312?23131312312?23131312312?");

        //Assert & Act
        assertThrows(PersistenceException.class, () -> {
            UserEntity storedUserEntity = testEntityManager.persistAndFlush(userEntity);
        }, "Was expecting a Persistance Exception to be thrown");

    }


}
