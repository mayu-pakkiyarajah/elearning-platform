package com.elearning.course.mapper;

import com.elearning.course.dto.response.CourseFileResponse;
import com.elearning.course.entity.CourseFile;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseFileMapper {

    @Mapping(target = "lessonId", expression = "java(file.getLesson() != null ? file.getLesson().getId() : null)")
    CourseFileResponse toResponse(CourseFile file);
}
