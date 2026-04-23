package com.product_service.product_microservice.service;

import com.product_service.product_microservice.entity.Product;
import com.product_service.product_microservice.exception.ProductNotFoundException;
import com.product_service.product_microservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void addProduct_Success() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setQuantity(10);

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Laptop");
        savedProduct.setPrice(999.99);
        savedProduct.setQuantity(10);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.addProduct(product);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void addProduct_NullName_ThrowsException() {
        Product product = new Product();
        product.setPrice(999.99);
        product.setQuantity(10);

        assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product));
        verify(productRepository, never()).save(any());
    }

    @Test
    void addProduct_NegativePrice_ThrowsException() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(-100.0);
        product.setQuantity(10);

        assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product));
        verify(productRepository, never()).save(any());
    }

    @Test
    void addProduct_NegativeQuantity_ThrowsException() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setQuantity(-5);

        assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product));
        verify(productRepository, never()).save(any());
    }

    @Test
    void getAllProducts_Success() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");

        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void getProductById_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1L));
        verify(productRepository).findById(1L);
    }

    @Test
    void updateProduct_Success() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Laptop");
        existingProduct.setPrice(999.99);
        existingProduct.setQuantity(10);

        Product updateData = new Product();
        updateData.setName("Updated Laptop");
        updateData.setPrice(1099.99);
        updateData.setQuantity(15);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        Product result = productService.updateProduct(1L, updateData);

        assertNotNull(result);
        assertEquals("Updated Laptop", result.getName());
        assertEquals(1099.99, result.getPrice());
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        Product updateData = new Product();
        updateData.setName("Updated Laptop");

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(1L, updateData));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_Success() {
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository).deleteById(1L);
    }
}
