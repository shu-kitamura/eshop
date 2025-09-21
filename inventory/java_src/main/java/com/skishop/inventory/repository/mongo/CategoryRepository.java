package com.skishop.inventory.repository.mongo;

import com.skishop.inventory.entity.mongo.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Category repository (MongoDB)
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    /**
     * Find category by name
     */
    Optional<Category> findByNameAndActiveTrue(String name);

    /**
     * Find categories by parent category ID
     */
    List<Category> findByParentIdAndActiveTrueOrderByName(String parentId);

    /**
     * Find root categories
     */
    List<Category> findByParentIdIsNullAndActiveTrueOrderByName();

    /**
     * Find categories by level
     */
    List<Category> findByLevelAndActiveTrueOrderByName(Integer level);

    /**
     * Get all active categories
     */
    List<Category> findByActiveTrueOrderByPathAsc();

    /**
     * Find category by path
     */
    Optional<Category> findByPathAndActiveTrue(String path);

    /**
     * Search categories by partial match of hierarchical path
     */
    @Query("{'path': {$regex: ?0, $options: 'i'}, 'active': true}")
    List<Category> findByPathContainingIgnoreCase(String pathPart);

    /**
     * Check if child categories exist
     */
    boolean existsByParentIdAndActiveTrue(String parentId);

    /**
     * Get parent category and all its child categories
     */
    @Query("{'path': {$regex: ?0}, 'active': true}")
    List<Category> findCategoryHierarchy(String parentPath);
}
