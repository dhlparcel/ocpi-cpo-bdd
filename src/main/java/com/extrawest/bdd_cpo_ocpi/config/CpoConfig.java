package com.extrawest.bdd_cpo_ocpi.config;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class CpoConfig {
    @ConfigProperty(name = "cpo.token.a")
    private String tokenA;
    @ConfigProperty(name = "cpo.version.url")
    private String versionUrl;

    public String getTokenA() {
        return tokenA;
    }

    public String getVersionUrl() {
        return versionUrl;
    }
}
