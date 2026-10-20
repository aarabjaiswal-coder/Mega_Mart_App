package com.megamart.repository;

import com.megamart.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByActiveTrueAndCategoryIgnoreCase(String category, Pageable pageable);
    Optional<Product> findBySkuIgnoreCase(String sku);

    @Query("select distinct product.category from Product product where product.active = true order by product.category")
    List<String> findActiveCategories();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id and product.active = true")
    Optional<Product> findActiveByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where upper(product.sku) = upper(:sku)")
    Optional<Product> findBySkuForUpdate(@Param("sku") String sku);
}
