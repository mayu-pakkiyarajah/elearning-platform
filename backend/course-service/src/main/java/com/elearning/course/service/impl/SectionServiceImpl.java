package com.elearning.course.service.impl;

import com.elearning.course.dto.request.SectionRequest;
import com.elearning.course.dto.response.SectionResponse;
import com.elearning.course.entity.Course;
import com.elearning.course.entity.Section;
import com.elearning.course.exception.ForbiddenOperationException;
import com.elearning.course.exception.ResourceNotFoundException;
import com.elearning.course.mapper.SectionMapper;
import com.elearning.course.repository.CourseRepository;
import com.elearning.course.repository.SectionRepository;
import com.elearning.course.security.AuthenticatedUser;
import com.elearning.course.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final SectionMapper sectionMapper;

    @Override
    @Transactional
    public SectionResponse create(Long courseId, SectionRequest request, AuthenticatedUser currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        assertOwnerOrAdmin(course, currentUser);

        Section section = Section.builder()
                .course(course)
                .title(request.getTitle())
                .position(request.getPosition())
                .build();

        return sectionMapper.toResponse(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public SectionResponse update(Long sectionId, SectionRequest request, AuthenticatedUser currentUser) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
        assertOwnerOrAdmin(section.getCourse(), currentUser);

        section.setTitle(request.getTitle());
        section.setPosition(request.getPosition());

        return sectionMapper.toResponse(section);
    }

    @Override
    @Transactional
    public void delete(Long sectionId, AuthenticatedUser currentUser) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
        assertOwnerOrAdmin(section.getCourse(), currentUser);
        sectionRepository.delete(section);
    }

    private void assertOwnerOrAdmin(Course course, AuthenticatedUser currentUser) {
        boolean isOwner = course.isOwnedBy(currentUser.userId());
        boolean isAdmin = currentUser.hasRole(ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("You do not have permission to modify this course's sections");
        }
    }
}
