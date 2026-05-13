package com.product_service.product_microservice.repository;

import com.product_service.product_microservice.entity.VendorRequest;
import com.product_service.product_microservice.enums.VendorRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRequestRepository extends JpaRepository<VendorRequest, Long> {

    List<VendorRequest> findByStatus(VendorRequestStatus status);

    Optional<VendorRequest> findByVendorIdAndStatus(Long vendorId, VendorRequestStatus status);

    boolean existsByVendorIdAndStatus(Long vendorId, VendorRequestStatus status);
}
