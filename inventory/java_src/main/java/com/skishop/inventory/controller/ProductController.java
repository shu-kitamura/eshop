package com.skishop.inventory.controller;

import com.skishop.inventory.dto.ProductDTO;
import com.skishop.inventory.dto.request.ProductCreateRequest;
import com.skishop.inventory.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * Product API Controller
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Get product list
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Product list request - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.findAll(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        log.info("Product search request - keyword: {}, page: {}, size: {}", keyword, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String id) {
        log.info("Product detail request - ID: {}", id);
        
        ProductDTO product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        log.info("Get product by SKU request - SKU: {}", sku);
        
        ProductDTO product = productService.findBySku(sku);
        return ResponseEntity.ok(product);
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        log.info("Get products by category request - categoryId: {}, page: {}, size: {}", categoryId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.findByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get multiple products in batch
     */
    @PostMapping("/batch")
    public ResponseEntity<List<ProductDTO>> getProductsByIds(@RequestBody List<String> ids) {
        log.info("Batch get products request - IDs: {}", ids);
        
        List<ProductDTO> products = productService.findByIds(ids);
        return ResponseEntity.ok(products);
    }

    /**
     * Create product
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.info("Create product request - SKU: {}", request.getSku());
        
        ProductDTO product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
}
