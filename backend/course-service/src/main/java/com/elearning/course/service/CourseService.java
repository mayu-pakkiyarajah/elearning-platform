package com.elearning.course.service;

import com.elearning.course.dto.request.CourseRequest;
import com.elearning.course.dto.response.CourseResponse;
import com.elearning.course.dto.response.CourseSummaryResponse;
import com.elearning.course.dto.response.PageResponse;
import com.elearning.course.security.AuthenticatedUser;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    PageResponse<CourseSummaryResponse> browse(
            Long categoryId, String level, String language, String search, Pageable pageable);

    CourseResponse getBySlug(String slug);

    java.util.List<CourseResponse> listMine(AuthenticatedUser currentUser);

    CourseResponse create(CourseRequest request, AuthenticatedUser currentUser);

    CourseResponse update(Long courseId, CourseRequest request, AuthenticatedUser currentUser);

    void delete(Long courseId, AuthenticatedUser currentUser);

    CourseResponse setPublished(Long courseId, boolean published, AuthenticatedUser currentUser);
}
