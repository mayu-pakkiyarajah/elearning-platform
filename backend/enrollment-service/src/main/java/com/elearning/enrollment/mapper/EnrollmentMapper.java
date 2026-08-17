package com.elearning.enrollment.mapper;

import com.elearning.enrollment.dto.response.EnrollmentResponse;
import com.elearning.enrollment.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    @Mapping(target = "status", expression = "java(enrollment.getStatus().name())")
    @Mapping(target = "completedLessons", expression = "java(enrollment.completedLessonCount())")
    @Mapping(target = "progressPercent", expression = "java(enrollment.progressPercent())")
    EnrollmentResponse toResponse(Enrollment enrollment);
}
