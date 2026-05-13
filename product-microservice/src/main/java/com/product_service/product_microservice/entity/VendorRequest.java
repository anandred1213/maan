package com.product_service.product_microservice.entity;

import com.product_service.product_microservice.enums.VendorRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_requests")
public class VendorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @Column(nullable = false)
    @NotBlank(message = "Business name is required")
    private String businessName;

    @Column(nullable = false)
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    @Column(length = 1000)
    private String businessDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorRequestStatus status = VendorRequestStatus.PENDING;

    private String adminRemarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    public VendorRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getBusinessDescription() { return businessDescription; }
    public void setBusinessDescription(String businessDescription) { this.businessDescription = businessDescription; }

    public VendorRequestStatus getStatus() { return status; }
    public void setStatus(VendorRequestStatus status) { this.status = status; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public LocalDateTime getRequestedAt() { return requestedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
