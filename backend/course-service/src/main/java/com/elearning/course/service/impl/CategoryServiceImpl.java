package com.elearning.course.service.impl;

import com.elearning.course.dto.request.CategoryRequest;
import com.elearning.course.dto.response.CategoryResponse;
import com.elearning.course.entity.Category;
import com.elearning.course.exception.DuplicateResourceException;
import com.elearning.course.exception.ResourceNotFoundException;
import com.elearning.course.mapper.CategoryMapper;
import com.elearning.course.repository.CategoryRepository;
import com.elearning.course.service.CategoryService;
import com.elearning.course.service.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A category named '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(SlugUtil.uniqueSlugify(request.getName(), s -> categoryRepository.findBySlug(s).isPresent()))
                .description(request.getDescription())
                .build();

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
