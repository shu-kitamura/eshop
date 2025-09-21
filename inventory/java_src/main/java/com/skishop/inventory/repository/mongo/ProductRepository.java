package com.skishop.inventory.repository.mongo;

import com.skishop.inventory.entity.mongo.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product repository (MongoDB)
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find products by category ID
     */
    Page<Product> findByCategoryIdAndActiveTrue(String categoryId, Pageable pageable);

    /**
     * Find active products
     */
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Find products by brand
     */
    Page<Product> findByBrandAndActiveTrue(String brand, Pageable pageable);

    /**
     * Search products by partial match of product name
     */
    @Query("{'name': {$regex: ?0, $options: 'i'}, 'active': true}")
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Search products by tags
     */
    Page<Product> findByTagsInAndActiveTrue(List<String> tags, Pageable pageable);

    /**
     * Composite search (name, description, brand, tags) - Uses Java 21 text block
     */
    @Query("""
           {
               $and: [
                   {'active': true},
                   {
                       $or: [
                           {'name': {$regex: ?0, $options: 'i'}},
                           {'description': {$regex: ?0, $options: 'i'}},
                           {'brand': {$regex: ?0, $options: 'i'}},
                           {'tags': {$in: [?0]}}
                       ]
                   }
               ]
           }
           """)
    Page<Product> searchProducts(String keyword, Pageable pageable);

    /**
     * Composite search by category and keyword - Uses Java 21 text block
     */
    @Query("""
           {
               $and: [
                   {'categoryId': ?0},
                   {'active': true},
                   {
                       $or: [
                           {'name': {$regex: ?1, $options: 'i'}},
                           {'description': {$regex: ?1, $options: 'i'}},
                           {'brand': {$regex: ?1, $options: 'i'}},
                           {'tags': {$in: [?1]}}
                       ]
                   }
               ]
           }
           """)
    Page<Product> searchProductsByCategory(String categoryId, String keyword, Pageable pageable);

    /**
     * Find multiple products by a list of product IDs
     */
    List<Product> findByIdInAndActiveTrue(List<String> ids);

    /**
     * Get list of distinct brands
     */
    @Query(value = "{}", fields = "{'brand': 1}")
    List<String> findDistinctBrands();

    /**
     * Check if a product exists by SKU and is active
     */
    boolean existsBySkuAndActiveTrue(String sku);
}
