package com.product_service.product_microservice.controller;

import com.product_service.product_microservice.dto.*;
import com.product_service.product_microservice.entity.Product;
import com.product_service.product_microservice.entity.VendorRequest;
import com.product_service.product_microservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Product API", description = "E-commerce product management")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ─── VENDOR: Apply to sell on platform ───────────────────────────────────

    @PostMapping("/vendor/apply")
    @Operation(summary = "Vendor applies to sell on the platform")
    public ResponseEntity<VendorRequest> applyAsVendor(@Valid @RequestBody VendorRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.applyAsVendor(dto));
    }

    // ─── VENDOR: Product management ──────────────────────────────────────────

    @PostMapping("/vendor/products")
    @Operation(summary = "Vendor submits a product for admin approval")
    public ResponseEntity<Product> submitProduct(@Valid @RequestBody ProductRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.submitProduct(dto));
    }

    @GetMapping("/vendor/{vendorId}/products")
    @Operation(summary = "Vendor views their own products")
    public ResponseEntity<List<Product>> getVendorProducts(@PathVariable Long vendorId) {
        return ResponseEntity.ok(productService.getVendorProducts(vendorId));
    }

    @PutMapping("/vendor/{vendorId}/products/{productId}")
    @Operation(summary = "Vendor updates their product (resets to PENDING for re-approval)")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long vendorId,
            @PathVariable Long productId,
            @RequestBody ProductRequest dto) {
        return ResponseEntity.ok(productService.updateProduct(productId, vendorId, dto));
    }

    @DeleteMapping("/vendor/{vendorId}/products/{productId}")
    @Operation(summary = "Vendor deletes their product")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long vendorId,
            @PathVariable Long productId) {
        productService.deleteProduct(productId, vendorId);
        return ResponseEntity.noContent().build();
    }

    // ─── ADMIN: Vendor request management ────────────────────────────────────

    @GetMapping("/admin/vendor-requests")
    @Operation(summary = "Admin views all vendor requests")
    public ResponseEntity<List<VendorRequest>> getAllVendorRequests() {
        return ResponseEntity.ok(productService.getAllVendorRequests());
    }

    @GetMapping("/admin/vendor-requests/pending")
    @Operation(summary = "Admin views pending vendor requests")
    public ResponseEntity<List<VendorRequest>> getPendingVendorRequests() {
        return ResponseEntity.ok(productService.getPendingVendorRequests());
    }

    @PutMapping("/admin/vendor-requests/{requestId}/review")
    @Operation(summary = "Admin approves or rejects a vendor request")
    public ResponseEntity<VendorRequest> reviewVendorRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody AdminReviewDto dto) {
        return ResponseEntity.ok(productService.reviewVendorRequest(requestId, dto));
    }

    // ─── ADMIN: Product approval management ──────────────────────────────────

    @GetMapping("/admin/products")
    @Operation(summary = "Admin views all products regardless of status")
    public ResponseEntity<List<Product>> getAllProductsForAdmin() {
        return ResponseEntity.ok(productService.getAllProductsForAdmin());
    }

    @GetMapping("/admin/products/pending")
    @Operation(summary = "Admin views products pending approval")
    public ResponseEntity<List<Product>> getPendingProducts() {
        return ResponseEntity.ok(productService.getPendingProducts());
    }

    @PutMapping("/admin/products/{productId}/review")
    @Operation(summary = "Admin approves or rejects a product")
    public ResponseEntity<Product> reviewProduct(
            @PathVariable Long productId,
            @Valid @RequestBody AdminReviewDto dto) {
        return ResponseEntity.ok(productService.reviewProduct(productId, dto));
    }

    @DeleteMapping("/admin/products/{productId}")
    @Operation(summary = "Admin deletes any product")
    public ResponseEntity<Void> adminDeleteProduct(@PathVariable Long productId) {
        productService.adminDeleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // ─── CUSTOMER: Browse approved products ──────────────────────────────────

    @GetMapping("/products")
    @Operation(summary = "Customer views all approved products")
    public ResponseEntity<List<ProductResponse>> getApprovedProducts() {
        return ResponseEntity.ok(productService.getApprovedProducts());
    }

    @GetMapping("/products/available")
    @Operation(summary = "Customer views approved products with stock > 0")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        return ResponseEntity.ok(productService.getAvailableProducts());
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Customer views a single approved product")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getApprovedProductById(id));
    }

    @GetMapping("/products/category/{category}")
    @Operation(summary = "Customer browses approved products by category")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getApprovedProductsByCategory(category));
    }

    // ─── CUSTOMER: Purchase ───────────────────────────────────────────────────

    @PostMapping("/products/{productId}/buy")
    @Operation(summary = "Customer purchases a product (real-time stock deduction)")
    public ResponseEntity<ProductResponse> purchaseProduct(
            @PathVariable Long productId,
            @Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(productService.purchaseProduct(productId, request));
    }
}
