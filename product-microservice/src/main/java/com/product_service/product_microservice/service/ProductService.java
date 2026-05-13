package com.product_service.product_microservice.service;

import com.product_service.product_microservice.dto.*;
import com.product_service.product_microservice.entity.Product;
import com.product_service.product_microservice.entity.VendorRequest;
import com.product_service.product_microservice.enums.ProductStatus;
import com.product_service.product_microservice.enums.VendorRequestStatus;
import com.product_service.product_microservice.exception.*;
import com.product_service.product_microservice.repository.ProductRepository;
import com.product_service.product_microservice.repository.VendorRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final VendorRequestRepository vendorRequestRepository;

    public ProductService(ProductRepository productRepository, VendorRequestRepository vendorRequestRepository) {
        this.productRepository = productRepository;
        this.vendorRequestRepository = vendorRequestRepository;
    }

    // ─── VENDOR: Apply to sell on platform ───────────────────────────────────

    public VendorRequest applyAsVendor(VendorRequestDto dto) {
        if (vendorRequestRepository.existsByVendorIdAndStatus(dto.getVendorId(), VendorRequestStatus.APPROVED)) {
            throw new IllegalStateException("Vendor ID " + dto.getVendorId() + " is already an approved vendor.");
        }
        if (vendorRequestRepository.existsByVendorIdAndStatus(dto.getVendorId(), VendorRequestStatus.PENDING)) {
            throw new IllegalStateException("A pending request already exists for vendor ID " + dto.getVendorId());
        }
        VendorRequest request = new VendorRequest();
        request.setVendorId(dto.getVendorId());
        request.setBusinessName(dto.getBusinessName());
        request.setContactEmail(dto.getContactEmail());
        request.setBusinessDescription(dto.getBusinessDescription());
        log.info("Vendor application submitted for vendorId={}", dto.getVendorId());
        return vendorRequestRepository.save(request);
    }

    // ─── ADMIN: Review vendor request ────────────────────────────────────────

    @Transactional
    public VendorRequest reviewVendorRequest(Long requestId, AdminReviewDto dto) {
        VendorRequest request = vendorRequestRepository.findById(requestId)
                .orElseThrow(() -> new VendorRequestNotFoundException(requestId));
        if (request.getStatus() != VendorRequestStatus.PENDING) {
            throw new IllegalStateException("Vendor request has already been reviewed.");
        }
        request.setStatus(VendorRequestStatus.valueOf(dto.getAction().toUpperCase()));
        request.setAdminRemarks(dto.getRemarks());
        request.setReviewedAt(LocalDateTime.now());
        log.info("Vendor request {} {} by admin", requestId, dto.getAction());
        return vendorRequestRepository.save(request);
    }

    public List<VendorRequest> getPendingVendorRequests() {
        return vendorRequestRepository.findByStatus(VendorRequestStatus.PENDING);
    }

    public List<VendorRequest> getAllVendorRequests() {
        return vendorRequestRepository.findAll();
    }

    // ─── VENDOR: Submit product (only approved vendors) ──────────────────────

    public Product submitProduct(ProductRequest dto) {
        boolean isApprovedVendor = vendorRequestRepository
                .existsByVendorIdAndStatus(dto.getVendorId(), VendorRequestStatus.APPROVED);
        if (!isApprovedVendor) {
            throw new AccessDeniedException("Vendor ID " + dto.getVendorId() + " is not approved to list products. Please apply first.");
        }
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setVendorId(dto.getVendorId());
        product.setStatus(ProductStatus.PENDING);
        log.info("Product '{}' submitted by vendorId={}, awaiting admin approval", dto.getName(), dto.getVendorId());
        return productRepository.save(product);
    }

    // ─── ADMIN: Approve or reject product ────────────────────────────────────

    @Transactional
    public Product reviewProduct(Long productId, AdminReviewDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (product.getStatus() != ProductStatus.PENDING) {
            throw new IllegalStateException("Product has already been reviewed.");
        }
        product.setStatus(ProductStatus.valueOf(dto.getAction().toUpperCase()));
        log.info("Product {} {} by admin", productId, dto.getAction());
        return productRepository.save(product);
    }

    public List<Product> getPendingProducts() {
        return productRepository.findByStatus(ProductStatus.PENDING);
    }

    // ─── CUSTOMER: Browse approved products ──────────────────────────────────

    public List<ProductResponse> getApprovedProducts() {
        return productRepository.findByStatus(ProductStatus.APPROVED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getAvailableProducts() {
        return productRepository.findAvailableProducts()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductResponse getApprovedProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new ProductNotFoundException(id);
        }
        return toResponse(product);
    }

    public List<ProductResponse> getApprovedProductsByCategory(String category) {
        return productRepository.findByCategoryAndStatus(category, ProductStatus.APPROVED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── CUSTOMER: Purchase product (real-time stock deduction) ──────────────

    @Transactional
    public ProductResponse purchaseProduct(Long productId, PurchaseRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new AccessDeniedException("Product is not available for purchase.");
        }
        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException(productId, request.getQuantity(), product.getStock());
        }
        product.setStock(product.getStock() - request.getQuantity());
        Product updated = productRepository.save(product);
        log.info("Customer {} purchased {} unit(s) of product {}. Remaining stock: {}",
                request.getCustomerId(), request.getQuantity(), productId, updated.getStock());
        return toResponse(updated);
    }

    // ─── VENDOR: Manage own products ─────────────────────────────────────────

    public List<Product> getVendorProducts(Long vendorId) {
        return productRepository.findByVendorId(vendorId);
    }

    @Transactional
    public Product updateProduct(Long productId, Long vendorId, ProductRequest dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (!product.getVendorId().equals(vendorId)) {
            throw new AccessDeniedException("You are not authorized to update this product.");
        }
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        // Reset to PENDING so admin re-approves after update
        product.setStatus(ProductStatus.PENDING);
        log.info("Product {} updated by vendorId={}, reset to PENDING for re-approval", productId, vendorId);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long productId, Long vendorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (!product.getVendorId().equals(vendorId)) {
            throw new AccessDeniedException("You are not authorized to delete this product.");
        }
        productRepository.delete(product);
        log.info("Product {} deleted by vendorId={}", productId, vendorId);
    }

    // ─── ADMIN: Full product management ──────────────────────────────────────

    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    @Transactional
    public void adminDeleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        productRepository.deleteById(productId);
        log.info("Product {} deleted by admin", productId);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private ProductResponse toResponse(Product p) {
        ProductResponse res = new ProductResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        res.setDescription(p.getDescription());
        res.setPrice(p.getPrice());
        res.setCurrency(p.getCurrency());
        res.setStock(p.getStock());
        res.setCategory(p.getCategory());
        res.setVendorId(p.getVendorId());
        res.setStatus(p.getStatus());
        res.setUpdatedAt(p.getUpdatedAt());
        return res;
    }
}
