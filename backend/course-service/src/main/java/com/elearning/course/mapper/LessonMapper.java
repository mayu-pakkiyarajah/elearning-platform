package com.elearning.course.mapper;

import com.elearning.course.dto.response.LessonResponse;
import com.elearning.course.entity.Lesson;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "contentType", expression = "java(lesson.getContentType().name())")
    LessonResponse toResponse(Lesson lesson);
}
