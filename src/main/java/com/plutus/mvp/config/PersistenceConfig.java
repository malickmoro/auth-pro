package com.plutus.mvp.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class PersistenceConfig {
    @Produces
    @PersistenceContext
    private EntityManager em;
}