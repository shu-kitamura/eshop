package com.skishop.inventory.controller;

import com.skishop.inventory.dto.CategoryDTO;
import com.skishop.inventory.dto.ProductDTO;
import com.skishop.inventory.dto.request.CategoryCreateRequest;
import com.skishop.inventory.dto.request.CategoryUpdateRequest;
import com.skishop.inventory.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Category Management API Controller
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Category API", description = "Product Category Management API")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get category list
     */
    @GetMapping
    @Operation(summary = "Get category list", description = "Retrieve a list of all categories")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved category list")
    })
    public ResponseEntity<Page<CategoryDTO>> getCategories(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String search) {
        
        log.info("Getting categories list with search: {}", search);
        Page<CategoryDTO> categories = categoryService.getCategories(pageable, search);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category details", description = "Retrieve details of the specified category ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved category details"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable String id) {
        
        log.info("Getting category detail for id: {}", id);
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Get list of products belonging to a category
     */
    @GetMapping("/{id}/products")
    @Operation(summary = "Get products by category", description = "Retrieve a list of products belonging to the specified category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product list"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable String id,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Only products in stock") @RequestParam(defaultValue = "false") boolean inStockOnly) {
        
        log.info("Getting products for category: {}, inStockOnly: {}", id, inStockOnly);
        Page<ProductDTO> products = categoryService.getProductsByCategory(id, pageable, inStockOnly);
        return ResponseEntity.ok(products);
    }

    /**
     * Create category
     */
    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "403", description = "Admin privileges required"),
        @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<CategoryDTO> createCategory(
            @Parameter(description = "Category create request") @Valid @RequestBody CategoryCreateRequest request) {
        
        log.info("Creating new category: {}", request.getName());
        CategoryDTO category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * Update category
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update the information of the specified category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "403", description = "Admin privileges required")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @Parameter(description = "Category ID") @PathVariable String id,
            @Parameter(description = "Category update request") @Valid @RequestBody CategoryUpdateRequest request) {
        
        log.info("Updating category: {}", id);
        CategoryDTO category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * Delete category
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete the specified category")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete because products are linked"),
        @ApiResponse(responseCode = "403", description = "Admin privileges required")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable String id) {
        
        log.info("Deleting category: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
