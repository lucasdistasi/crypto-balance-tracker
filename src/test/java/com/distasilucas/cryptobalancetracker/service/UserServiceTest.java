package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.User;
import com.distasilucas.cryptobalancetracker.exception.UsernameNotFoundException;
import com.distasilucas.cryptobalancetracker.model.Role;
import com.distasilucas.cryptobalancetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.USERNAME_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class UserServiceTest {

    @Mock
    private UserRepository userRepositoryMock;

    private UserService userService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        userService = new UserService(userRepositoryMock);
    }

    @Test
    void shouldFindUserByUsername() {
        var entityUser = new User(UUID.randomUUID().toString(), "admin", "admin", Role.ROLE_ADMIN, LocalDateTime.now());

        when(userRepositoryMock.findByUsername("admin")).thenReturn(Optional.of(entityUser));

        var user = userService.findByUsername("admin");

        assertThat(user)
                .usingRecursiveAssertion()
                .isEqualTo(entityUser);
        assertEquals("admin", user.getUsername());
        assertEquals("admin", user.getPassword());
        assertEquals(List.of(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name())), user.getAuthorities());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionIfUserDoesNotExists() {
        when(userRepositoryMock.findByUsername("admin")).thenReturn(Optional.empty());

        var exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.findByUsername("admin")
        );

        assertEquals(USERNAME_NOT_FOUND.formatted("admin"), exception.getMessage());
    }

}