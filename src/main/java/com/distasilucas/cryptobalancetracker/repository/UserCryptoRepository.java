package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserCryptoRepository extends MongoRepository<UserCrypto, String> {

    List<UserCrypto> findAllByCoingeckoCryptoId(String coingeckoCryptoId);

    Optional<UserCrypto> findByCoingeckoCryptoIdAndPlatformId(String coingeckoCryptoId, String platformId);

    List<UserCrypto> findAllByPlatformId(String platformId);
}
