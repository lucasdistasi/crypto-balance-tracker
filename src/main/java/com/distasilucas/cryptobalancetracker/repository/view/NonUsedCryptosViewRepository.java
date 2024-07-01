package com.distasilucas.cryptobalancetracker.repository.view;

import com.distasilucas.cryptobalancetracker.entity.view.NonUsedCryptosView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NonUsedCryptosViewRepository extends JpaRepository<NonUsedCryptosView, String> {

    @Query("""
            SELECT nonUsedCryptos
            FROM NonUsedCryptosView nonUsedCryptos
            WHERE nonUsedCryptos.id = :coingeckoCryptoId
        """)
    Optional<NonUsedCryptosView> findNonUsedCryptosByCoingeckoCryptoId(String coingeckoCryptoId);
}
