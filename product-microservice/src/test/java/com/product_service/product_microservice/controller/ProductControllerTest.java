package com.product_service.product_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product_service.product_microservice.entity.Product;
import com.product_service.product_microservice.exception.ProductNotFoundException;
import com.product_service.product_microservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addProduct_Success() throws Exception {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setQuantity(10);

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Laptop");
        savedProduct.setPrice(999.99);
        savedProduct.setQuantity(10);

        when(productService.addProduct(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService).addProduct(any(Product.class));
    }

    @Test
    void getAllProducts_Success() throws Exception {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");

        List<Product> products = Arrays.asList(product1, product2);

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Mouse"));

        verify(productService).getAllProducts();
    }

    @Test
    void getProductById_Success() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService).getProductById(1L);
    }

    @Test
    void updateProduct_Success() throws Exception {
        Product product = new Product();
        product.setName("Updated Laptop");
        product.setPrice(1099.99);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setPrice(1099.99);

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/products/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));

        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void deleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/delete/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }
}
