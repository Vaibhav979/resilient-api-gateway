package com.infra.api_gateway.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infra.api_gateway.entities.CachedResponse;

public interface CachedResponseRepository extends JpaRepository<CachedResponse, String> {
}
