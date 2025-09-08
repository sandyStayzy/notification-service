package com.notification.system.model.entity;

import com.notification.system.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "password123");
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals(UserRole.USER, user.getRole());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void testUserDefaultConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getPassword());
        assertEquals(UserRole.USER, emptyUser.getRole());
    }

    @Test
    void testSettersAndGetters() {
        user.setId(100L);
        user.setUsername("newusername");
        user.setEmail("new@example.com");
        user.setPassword("newpassword");
        user.setRole(UserRole.ADMIN);
        user.setPhoneNumber("+1234567890");
        
        assertEquals(100L, user.getId());
        assertEquals("newusername", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals("+1234567890", user.getPhoneNumber());
    }

    @Test
    void testUserRoles() {
        assertEquals(UserRole.USER, user.getRole());
        
        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void testPhoneNumber() {
        assertNull(user.getPhoneNumber());
        
        user.setPhoneNumber("+1234567890");
        assertEquals("+1234567890", user.getPhoneNumber());
        
        user.setPhoneNumber(null);
        assertNull(user.getPhoneNumber());
    }

}