package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlatformRepository extends MongoRepository<Platform, String> {

    Optional<Platform> findByName(String name);
    List<Platform> findAllByIdIn(Collection<String> ids);
}
