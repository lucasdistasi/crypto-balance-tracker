package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserCryptoRepository extends JpaRepository<UserCrypto, String> {

    @Query(
        value = """
            SELECT user_cryptos
            FROM UserCrypto user_cryptos
            WHERE user_cryptos.crypto.id = :coingeckoCryptoId
            """
    )
    List<UserCrypto> findAllByCoingeckoCryptoId(String coingeckoCryptoId);

    @Query(
        value = """
            SELECT user_cryptos
            FROM UserCrypto user_cryptos
            WHERE user_cryptos.crypto.id = :coingeckoCryptoId
            AND user_cryptos.platform.id = :platformId
            """
    )
    Optional<UserCrypto> findByCoingeckoCryptoIdAndPlatformId(String coingeckoCryptoId, String platformId);

    @Query(
        value = """
            SELECT user_cryptos
            FROM UserCrypto user_cryptos
            WHERE user_cryptos.platform.id = :platformId
            """
    )
    List<UserCrypto> findAllByPlatformId(String platformId);
}
