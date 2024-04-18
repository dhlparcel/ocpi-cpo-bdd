package com.extrawest.bdd_cpo_ocpi.config;

import jakarta.inject.Singleton;

@Singleton
public class EmspConfig {
    //    @Value("${emsp.version.url}")
    private String versionUrl;
    //    @Value("${emsp.token.a}")
    private String tokenA;
    //    @Value("${emsp.role}")
    private String role;
    //    @Value("${emsp.party_id}")
    private String partyId;
    //    @Value("${emsp.country_code}")
    private String countryCode;

    public String getCountryCode() {
        return countryCode;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getRole() {
        return role;
    }

    public String getTokenA() {
        return tokenA;
    }

    public String getVersionUrl() {
        return versionUrl;
    }
}
