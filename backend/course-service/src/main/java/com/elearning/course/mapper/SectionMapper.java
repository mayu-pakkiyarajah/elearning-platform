package com.elearning.course.mapper;

import com.elearning.course.dto.response.SectionResponse;
import com.elearning.course.entity.Section;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = LessonMapper.class)
public interface SectionMapper {
    SectionResponse toResponse(Section section);
}
