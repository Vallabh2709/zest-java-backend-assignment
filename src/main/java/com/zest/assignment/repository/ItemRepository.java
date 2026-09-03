package com.zest.assignment.repository;

import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByProduct(Product product, Pageable pageable);

    boolean existsByProduct(Product product);
}