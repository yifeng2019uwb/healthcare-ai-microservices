package com.healthcare.dao;

import com.healthcare.entity.User;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserDao} — mocked, no DB required.
 */
@ExtendWith(MockitoExtension.class)
class UserDaoTest {

    @Mock
    private UserDao userDao;

    private User newUser() {
        return new User("john_doe", "john@example.com", "$2a$10$hash", UserRole.PATIENT);
    }

    @Test
    void findByUsername_returnsUser() {
        User user = newUser();
        when(userDao.findByUsername("john_doe")).thenReturn(Optional.of(user));

        Optional<User> result = userDao.findByUsername("john_doe");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
        verify(userDao).findByUsername("john_doe");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotFound() {
        when(userDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThat(userDao.findByUsername("unknown")).isEmpty();
    }

    @Test
    void findByEmail_returnsUser() {
        User user = newUser();
        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userDao.findByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        when(userDao.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThat(userDao.findByEmail("unknown@example.com")).isEmpty();
    }

    @Test
    void existsByUsername_returnsTrue() {
        when(userDao.existsByUsername("john_doe")).thenReturn(true);

        assertThat(userDao.existsByUsername("john_doe")).isTrue();
    }

    @Test
    void existsByUsername_returnsFalse_whenNotFound() {
        when(userDao.existsByUsername("unknown")).thenReturn(false);

        assertThat(userDao.existsByUsername("unknown")).isFalse();
    }

    @Test
    void existsByEmail_returnsTrue() {
        when(userDao.existsByEmail("john@example.com")).thenReturn(true);

        assertThat(userDao.existsByEmail("john@example.com")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse_whenNotFound() {
        when(userDao.existsByEmail("unknown@example.com")).thenReturn(false);

        assertThat(userDao.existsByEmail("unknown@example.com")).isFalse();
    }

    @Test
    void save_returnsUser() {
        User user = newUser();
        when(userDao.save(user)).thenReturn(user);

        User saved = userDao.save(user);

        assertThat(saved).isEqualTo(user);
        verify(userDao).save(user);
    }

    @Test
    void findById_returnsUser() {
        UUID id = UUID.randomUUID();
        User user = newUser();
        when(userDao.findById(id)).thenReturn(Optional.of(user));

        assertThat(userDao.findById(id)).isPresent();
    }
}