package com.product_service.product_microservice.repository;

import com.product_service.product_microservice.entity.Product;
import com.product_service.product_microservice.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByVendorId(Long vendorId);

    List<Product> findByVendorIdAndStatus(Long vendorId, ProductStatus status);

    List<Product> findByCategoryAndStatus(String category, ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND p.stock > 0")
    List<Product> findAvailableProducts();
}
