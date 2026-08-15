package com.elearning.course.repository;

import com.elearning.course.entity.Course;
import com.elearning.course.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Course> findByInstructorId(Long instructorId);

    @Query("""
        SELECT c FROM Course c
        WHERE c.status = :status
          AND (:categoryId IS NULL OR c.category.id = :categoryId)
          AND (:level IS NULL OR c.level = :level)
          AND (:language IS NULL OR c.language = :language)
          AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Course> search(
            @Param("status") CourseStatus status,
            @Param("categoryId") Long categoryId,
            @Param("level") com.elearning.course.entity.CourseLevel level,
            @Param("language") String language,
            @Param("search") String search,
            Pageable pageable
    );
}
