package com.elearning.course.service;

import com.elearning.course.dto.request.LessonRequest;
import com.elearning.course.dto.response.LessonResponse;
import com.elearning.course.security.AuthenticatedUser;

public interface LessonService {
    LessonResponse create(Long sectionId, LessonRequest request, AuthenticatedUser currentUser);
    LessonResponse update(Long lessonId, LessonRequest request, AuthenticatedUser currentUser);
    void delete(Long lessonId, AuthenticatedUser currentUser);
}
