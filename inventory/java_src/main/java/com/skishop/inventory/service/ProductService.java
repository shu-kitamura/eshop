package com.skishop.inventory.service;

import com.skishop.inventory.dto.ProductDTO;
import com.skishop.inventory.dto.CategoryDTO;
import com.skishop.inventory.dto.request.ProductCreateRequest;
import com.skishop.inventory.entity.mongo.Product;
import com.skishop.inventory.entity.jpa.Inventory;
import com.skishop.inventory.entity.jpa.Price;
import com.skishop.inventory.repository.mongo.ProductRepository;
import com.skishop.inventory.repository.jpa.InventoryRepository;
import com.skishop.inventory.repository.jpa.PriceRepository;
import com.skishop.inventory.mapper.ProductMapper;
import com.skishop.inventory.exception.ResourceNotFoundException;
import com.skishop.inventory.exception.DuplicateResourceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Product Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PriceRepository priceRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final EventPublisherService eventPublisherService;

    /**
     * Get product list
     */
    @Cacheable(value = "products", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductDTO> findAll(Pageable pageable) {
        log.debug("Get product list - Page: {}", pageable);
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return products.map(this::enrichProductWithDetails);
    }

    /**
     * Search products
     */
    @Cacheable(value = "productSearch", key = "#keyword + '_' + #pageable.pageNumber")
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        log.debug("Search products - Keyword: {}, Page: {}", keyword, pageable);
        Page<Product> products = productRepository.searchProducts(keyword, pageable);
        return products.map(this::enrichProductWithDetails);
    }

    /**
     * Get product details
     */
    @Cacheable(value = "product", key = "#id")
    public ProductDTO findById(String id) {
        log.debug("Get product details - ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        
        return enrichProductWithDetails(product);
    }

    /**
     * Get product by SKU
     */
    @Cacheable(value = "productBySku", key = "#sku")
    public ProductDTO findBySku(String sku) {
        log.debug("Get product by SKU - SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + sku));
        
        return enrichProductWithDetails(product);
    }

    /**
     * Get products by category
     */
    @Cacheable(value = "productsByCategory", key = "#categoryId + '_' + #pageable.pageNumber")
    public Page<ProductDTO> findByCategory(String categoryId, Pageable pageable) {
        log.debug("Get products by category - Category ID: {}, Page: {}", categoryId, pageable);
        Page<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return products.map(this::enrichProductWithDetails);
    }

    /**
     * Create product
     */
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public ProductDTO createProduct(ProductCreateRequest request) {
        log.info("Start creating product - SKU: {}", request.getSku());

        // Check for duplicate SKU
        if (productRepository.existsBySkuAndActiveTrue(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }

        // Check if category exists
        categoryService.findById(request.getCategoryId());

        // Create product entity
        Product product = productMapper.toEntity(request);
        product.prePersist();
        product = productRepository.save(product);

        // Create price information
        Price price = Price.builder()
            .productId(product.getId())
            .regularPrice(request.getPrice().getRegularPrice())
            .salePrice(request.getPrice().getSalePrice())
            .saleStartDate(request.getPrice().getSaleStartDate())
            .saleEndDate(request.getPrice().getSaleEndDate())
            .currencyCode(request.getPrice().getCurrencyCode())
            .build();
        price.prePersist();
        priceRepository.save(price);

        // Create inventory information
        Inventory inventory = Inventory.builder()
            .productId(product.getId())
            .quantity(request.getInventory().getQuantity())
            .locationCode(request.getInventory().getLocationCode())
            .build();
        inventory.prePersist();
        inventoryRepository.save(inventory);

        // Publish event
        eventPublisherService.publishProductCreatedEvent(product.getId());

        log.info("Product creation completed - ID: {}, SKU: {}", product.getId(), product.getSku());
        return enrichProductWithDetails(product);
    }

    /**
     * Enrich product with additional details
     */
    private ProductDTO enrichProductWithDetails(Product product) {
        // Get category information
        CategoryDTO category = null;
        if (product.getCategoryId() != null) {
            try {
                category = categoryService.findById(product.getCategoryId());
            } catch (ResourceNotFoundException e) {
                log.warn("Category not found - ID: {}", product.getCategoryId());
            }
        }

        // Get price information (using Java 21 Optional improvements)
        ProductDTO.PriceInfoDTO priceInfo = priceRepository.findByProductIdAndIsActiveTrue(product.getId())
            .map(p -> new ProductDTO.PriceInfoDTO(
                p.getRegularPrice(),
                p.getSalePrice(),
                p.getCurrentPrice(),
                p.getCurrencyCode(),
                p.isOnSale(),
                p.getSaleStartDate(),
                p.getSaleEndDate()
            ))
            .orElse(null);

        // Get inventory information (using Java 21 Optional improvements)
        ProductDTO.InventoryInfoDTO inventoryInfo = inventoryRepository.findByProductId(product.getId())
            .map(inv -> new ProductDTO.InventoryInfoDTO(
                inv.getStatus().name(),
                inv.getQuantity(),
                inv.getAvailableQuantity(),
                inv.getLocationCode()
            ))
            .orElse(null);

        // Create ProductDTO (using record constructor)
        return new ProductDTO(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getBrand(),
            product.getAttributes(),
            product.getTags(),
            category,
            priceInfo,
            inventoryInfo,
            null, // images - implement as needed
            null, // imageUrl - implement as needed
            product.getActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    /**
     * Get details for multiple products in batch
     */
    public List<ProductDTO> findByIds(List<String> ids) {
        log.debug("Get multiple products - IDs: {}", ids);
        List<Product> products = productRepository.findByIdInAndActiveTrue(ids);
        return products.stream()
            .map(this::enrichProductWithDetails)
            .toList();
    }
}
