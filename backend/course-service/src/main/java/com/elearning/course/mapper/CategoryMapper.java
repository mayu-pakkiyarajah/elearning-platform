package com.elearning.course.mapper;

import com.elearning.course.dto.response.CategoryResponse;
import com.elearning.course.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
