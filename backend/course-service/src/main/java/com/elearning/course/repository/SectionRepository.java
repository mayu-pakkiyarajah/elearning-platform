package com.elearning.course.repository;

import com.elearning.course.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCourseIdOrderByPositionAsc(Long courseId);
}
