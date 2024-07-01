package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CryptoRepository extends JpaRepository<Crypto, String> {

    @Query(
        value = """
            SELECT cryptos
            FROM Crypto cryptos
            WHERE cryptos.lastUpdatedAt <= :dateFilter
            ORDER BY cryptos.lastUpdatedAt ASC
            LIMIT :limit
            """
    )
    List<Crypto> findOldestNCryptosByLastPriceUpdate(LocalDateTime dateFilter, int limit);

    List<Crypto> findAllByIdIn(Collection<String> ids);

}
