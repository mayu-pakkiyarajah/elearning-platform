package com.elearning.course.service;

import com.elearning.course.dto.request.SectionRequest;
import com.elearning.course.dto.response.SectionResponse;
import com.elearning.course.security.AuthenticatedUser;

public interface SectionService {
    SectionResponse create(Long courseId, SectionRequest request, AuthenticatedUser currentUser);
    SectionResponse update(Long sectionId, SectionRequest request, AuthenticatedUser currentUser);
    void delete(Long sectionId, AuthenticatedUser currentUser);
}
