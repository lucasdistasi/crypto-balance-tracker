package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CryptoRepository extends MongoRepository<Crypto, String> {

    @Aggregation(pipeline = {
            "{ $match: { 'last_updated_at': { $lte: ?0 } } }",
            "{ $sort: { 'last_updated_at': 1 } }",
            "{ $limit: ?1 }"
    })
    List<Crypto> findOldestNCryptosByLastPriceUpdate(LocalDateTime dateFilter, int limit);

    List<Crypto> findAllByIdIn(Collection<String> ids);

}
