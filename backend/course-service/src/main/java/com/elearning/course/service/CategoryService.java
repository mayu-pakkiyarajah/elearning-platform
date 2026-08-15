package com.elearning.course.service;

import com.elearning.course.dto.request.CategoryRequest;
import com.elearning.course.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> listAll();
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
}
