package com.infra.api_gateway.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "cached_responses")
public class CachedResponse {

    @Id
    private String cacheKey;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String jsonData;

    private Instant updatedAt;

    // Getters
    public String getCacheKey() {
        return cacheKey;
    }

    public String getJsonData() {
        return jsonData;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}