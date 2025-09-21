package com.skishop.inventory.service;

import com.skishop.inventory.dto.CategoryDTO;
import com.skishop.inventory.dto.ProductDTO;
import com.skishop.inventory.dto.request.CategoryCreateRequest;
import com.skishop.inventory.dto.request.CategoryUpdateRequest;
import com.skishop.inventory.entity.mongo.Category;
import com.skishop.inventory.repository.mongo.CategoryRepository;
import com.skishop.inventory.mapper.CategoryMapper;
import com.skishop.inventory.exception.ResourceNotFoundException;

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
 * Category Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Get all categories
     */
    @Cacheable(value = "categories")
    public List<CategoryDTO> findAll() {
        log.debug("Get all categories");
        List<Category> categories = categoryRepository.findByActiveTrueOrderByPathAsc();
        return categories.stream()
            .map(categoryMapper::toDTO)
            .toList();
    }

    /**
     * Get categories with pagination
     */
    public Page<CategoryDTO> getCategories(Pageable pageable, String searchKeyword) {
        log.debug("Get categories with pagination: keyword={}", searchKeyword);
        // Mock implementation
        List<CategoryDTO> mockCategories = findAll();
        return Page.empty();
    }

    /**
     * Get category by ID
     */
    public CategoryDTO getCategoryById(String id) {
        log.debug("Get category by ID: {}", id);
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return categoryMapper.toDTO(category);
    }

    /**
     * Get products by category
     */
    public Page<ProductDTO> getProductsByCategory(String categoryId, Pageable pageable, boolean includeInactive) {
        log.debug("Get products by category: categoryId={}, includeInactive={}", categoryId, includeInactive);
        // Mock implementation
        return Page.empty();
    }

    /**
     * Create category (from request)
     */
    @Transactional
    @CacheEvict(value = {"categories", "rootCategories"}, allEntries = true)
    public CategoryDTO createCategory(CategoryCreateRequest request) {
        log.info("Start creating category - Name: {}", request.name());
        
        // Create CategoryDTO from request
        CategoryDTO categoryDTO = new CategoryDTO(
            null,
            request.name(),
            request.description(),
            request.parentId(),
            null,
            List.of(),
            0,
            request.name(),
            true,
            null,
            null,
            0L
        );
        
        return create(categoryDTO);
    }

    /**
     * Create category
     */
    @Transactional
    @CacheEvict(value = {"categories", "rootCategories", "childCategories", "categoryHierarchy"}, allEntries = true)
    public CategoryDTO create(CategoryDTO categoryDTO) {
        log.info("Start creating category - Name: {}", categoryDTO.name());

        Category category = categoryMapper.toEntity(categoryDTO);
        
        // Set parent category information
        if (categoryDTO.parentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found: " + categoryDTO.parentId()));
            category.setLevel(parent.getLevel() + 1);
            category.setPath(parent.getPath() + "/" + category.getName());
        } else {
            category.setLevel(0);
            category.setPath(category.getName());
        }

        category.prePersist();
        category = categoryRepository.save(category);

        log.info("Category creation completed - ID: {}, Name: {}", category.getId(), category.getName());
        return categoryMapper.toDTO(category);
    }

    /**
     * Update category
     */
    @Transactional
    @CacheEvict(value = {"categories", "rootCategories"}, allEntries = true)
    public CategoryDTO updateCategory(String id, CategoryUpdateRequest request) {
        log.info("Start updating category - ID: {}, Name: {}", id, request.name());
        
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        
        // Update only basic fields (only those existing in the entity)
        existingCategory.setName(request.name());
        existingCategory.setDescription(request.description());
        existingCategory.setParentId(request.parentId());
        
        Category savedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDTO(savedCategory);
    }

    /**
     * Delete category
     */
    @Transactional
    @CacheEvict(value = {"categories", "rootCategories"}, allEntries = true)
    public void deleteCategory(String id) {
        log.info("Delete category - ID: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        
        // Logical delete
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Category deletion completed - ID: {}", id);
    }

    /**
     * Get category details
     */
    @Cacheable(value = "category", key = "#id")
    public CategoryDTO findById(String id) {
        log.debug("Get category details - ID: {}", id);
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        
        return enrichCategoryWithChildren(category);
    }

    /**
     * Get child categories
     */
    @Cacheable(value = "childCategories", key = "#parentId")
    public List<CategoryDTO> findByParentId(String parentId) {
        log.debug("Get child categories - Parent ID: {}", parentId);
        List<Category> categories = categoryRepository.findByParentIdAndActiveTrueOrderByName(parentId);
        return categories.stream()
            .map(categoryMapper::toDTO)
            .toList();
    }

    /**
     * Get category hierarchy
     */
    @Cacheable(value = "categoryHierarchy", key = "#parentPath")
    public List<CategoryDTO> findCategoryHierarchy(String parentPath) {
        log.debug("Get category hierarchy - Path: {}", parentPath);
        List<Category> categories = categoryRepository.findCategoryHierarchy("^" + parentPath);
        return categories.stream()
            .map(categoryMapper::toDTO)
            .toList();
    }

    /**
     * Enrich category with child category information
     */
    private CategoryDTO enrichCategoryWithChildren(Category category) {
        CategoryDTO dto = categoryMapper.toDTO(category);
        
        // Get child categories
        List<Category> children = categoryRepository.findByParentIdAndActiveTrueOrderByName(category.getId());
        List<CategoryDTO> childrenDTOs = children.stream()
            .map(categoryMapper::toDTO)
            .toList();
        
        // Create new DTO including child categories
        return new CategoryDTO(
            dto.id(),
            dto.name(),
            dto.description(),
            dto.parentId(),
            dto.parent(),
            childrenDTOs,
            dto.level(),
            dto.path(),
            dto.active(),
            dto.createdAt(),
            dto.updatedAt(),
            dto.productCount()
        );
    }
}
