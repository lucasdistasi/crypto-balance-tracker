package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "Platforms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Platform implements Serializable {

    @Id
    private String id;
    private String name;

    @OneToMany(mappedBy = "platform")
    private List<UserCrypto> userCryptos;

    public Platform(String id, String name) {
        this.id = id;
        this.name = name;
        this.userCryptos = Collections.emptyList();
    }

    public PlatformResponse toPlatformResponse() {
        return new PlatformResponse(id, name);
    }
}
