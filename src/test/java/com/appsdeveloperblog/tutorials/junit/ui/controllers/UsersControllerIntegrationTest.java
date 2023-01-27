package com.appsdeveloperblog.tutorials.junit.ui.controllers;

import com.appsdeveloperblog.tutorials.junit.security.SecurityConstants;
import com.appsdeveloperblog.tutorials.junit.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //Object instance keeps for the entire class and not for methods
public class UsersControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String authorizationToken;

    @Test
    @DisplayName("User can be created")
    @Order(1)
    void testCreateUser_whenValidDetailsProvided_returnsUsersDetails() throws JSONException {
        //Arrange
        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "Bruno");
        userDetailsRequestJson.put("lastName", "Barbosa");
        userDetailsRequestJson.put("email", "email@email.com");
        userDetailsRequestJson.put("password", "12345678");
        userDetailsRequestJson.put("repeatPassword", "12345678");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(userDetailsRequestJson.toString(), headers);

        //Act
        ResponseEntity<UserRest> createdUserDetailsEntity = testRestTemplate.postForEntity("/users", request, UserRest.class);
        UserRest createdUserDetails = createdUserDetailsEntity.getBody();

        //Assert
        assertEquals(HttpStatus.OK, createdUserDetailsEntity.getStatusCode());
        assertEquals(userDetailsRequestJson.getString("firstName"), createdUserDetails.getFirstName(), "The returned user's first name seems to be incorrect");
        assertEquals(userDetailsRequestJson.getString("lastName"), createdUserDetails.getLastName(), "The returned user's last name seems to be incorrect");
        assertEquals(userDetailsRequestJson.getString("email"), createdUserDetails.getEmail(), "The returned user's email seems to be incorrect");
        assertFalse(createdUserDetails.getUserId().trim().isEmpty(), "user id should not be empty");

    }

    @DisplayName("GET /users requires JWT")
    @Test
    @Order(2)
    void testGetUsers_whenMissingJWT_returns403() {
        //Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity requestEntity = new HttpEntity(null, headers);

        //Act
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<UserRest>>() {
        });

        //Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Http Status Code 403 should have been returned");
    }

    @DisplayName("Login works")
    @Test
    @Order(3)
    void testUserLogin_whenValidCredentialsProvided_returnsJWTinAuthorizationHeader() throws JSONException {
        //Arrange
        JSONObject loginCredentials = new JSONObject();
        loginCredentials.put("email", "email@email.com");
        loginCredentials.put("password", "12345678");

        HttpEntity<String> request = new HttpEntity<>(loginCredentials.toString());

        //Act
        ResponseEntity response = testRestTemplate.postForEntity("/users/login", request, null);
        authorizationToken = response.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING).get(0);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Http Status Code Should Be 200");
        assertNotNull(response.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING).get(0), "Response should contain Authorization header with JWT");
        assertNotNull(response.getHeaders().getValuesAsList("UserID").get(0), "Response should contain User ID in response header");
    }

    @Test
    @Order(4)
    @DisplayName("GET /users works")
    void testGetUsers_whenValidJWTProvided_returnsUsers() {
        //Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(authorizationToken);

        HttpEntity requestEntity = new HttpEntity(null, headers);

        //Act
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<UserRest>>() {
        });
        
        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Http Status should be 200 OK");
        assertTrue(response.getBody().size() == 1, "Should be exactly 1 user in the list");
    }

}
