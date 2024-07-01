package com.distasilucas.cryptobalancetracker.entity.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "NonUsedCryptosView")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NonUsedCryptosView {
    @Id
    private String id;
    private String name;
    private String ticker;
}
