package com.vinsguru.inventory.application.repository;

import com.vinsguru.inventory.application.entity.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Integer> {
}
