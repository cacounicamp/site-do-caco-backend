package com.caco.sitedocaco.features.forms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Conjunto de opções reutilizável entre perguntas e formulários (ex.: "sim-nao", "camiseta"). */
@Entity
@Getter
@Setter
@Table(name = "form_option_set", uniqueConstraints = @UniqueConstraint(name = "uk_form_option_set_slug", columnNames = "slug"))
public class OptionSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 60)
    private String slug;

    @Column(nullable = false, length = 120)
    private String name;
}
