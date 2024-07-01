package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCryptoRepository extends JpaRepository<UserCrypto, String> {

    List<UserCrypto> findAllByCoingeckoCryptoId(String coingeckoCryptoId);

    Optional<UserCrypto> findByCoingeckoCryptoIdAndPlatformId(String coingeckoCryptoId, String platformId);

    List<UserCrypto> findAllByPlatformId(String platformId);
}
