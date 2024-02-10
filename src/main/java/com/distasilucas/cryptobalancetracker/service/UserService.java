package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.User;
import com.distasilucas.cryptobalancetracker.exception.UsernameNotFoundException;
import com.distasilucas.cryptobalancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.USERNAME_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByUsername(String username) {
        var message = USERNAME_NOT_FOUND.formatted(username);

        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(message));
    }
}
