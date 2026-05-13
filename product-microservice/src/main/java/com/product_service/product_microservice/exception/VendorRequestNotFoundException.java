package com.product_service.product_microservice.exception;

public class VendorRequestNotFoundException extends RuntimeException {
    public VendorRequestNotFoundException(Long id) {
        super("Vendor request not found with ID: " + id);
    }
}
