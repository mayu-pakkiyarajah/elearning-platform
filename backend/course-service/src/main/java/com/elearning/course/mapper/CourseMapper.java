package com.elearning.course.mapper;

import com.elearning.course.dto.response.CourseResponse;
import com.elearning.course.dto.response.CourseSummaryResponse;
import com.elearning.course.entity.Course;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, SectionMapper.class})
public interface CourseMapper {

    @Mapping(target = "level", expression = "java(course.getLevel().name())")
    @Mapping(target = "status", expression = "java(course.getStatus().name())")
    CourseResponse toResponse(Course course);

    @Mapping(target = "level", expression = "java(course.getLevel().name())")
    @Mapping(target = "status", expression = "java(course.getStatus().name())")
    CourseSummaryResponse toSummaryResponse(Course course);
}
