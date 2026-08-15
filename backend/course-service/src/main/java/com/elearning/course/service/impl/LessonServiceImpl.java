package com.elearning.course.service.impl;

import com.elearning.course.dto.request.LessonRequest;
import com.elearning.course.dto.response.LessonResponse;
import com.elearning.course.entity.Course;
import com.elearning.course.entity.Lesson;
import com.elearning.course.entity.LessonContentType;
import com.elearning.course.entity.Section;
import com.elearning.course.exception.ForbiddenOperationException;
import com.elearning.course.exception.ResourceNotFoundException;
import com.elearning.course.mapper.LessonMapper;
import com.elearning.course.repository.LessonRepository;
import com.elearning.course.repository.SectionRepository;
import com.elearning.course.security.AuthenticatedUser;
import com.elearning.course.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final LessonMapper lessonMapper;

    @Override
    @Transactional
    public LessonResponse create(Long sectionId, LessonRequest request, AuthenticatedUser currentUser) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
        assertOwnerOrAdmin(section.getCourse(), currentUser);

        Lesson lesson = Lesson.builder()
                .section(section)
                .title(request.getTitle())
                .position(request.getPosition())
                .contentType(LessonContentType.valueOf(request.getContentType().toUpperCase()))
                .videoUrl(request.getVideoUrl())
                .durationSeconds(request.getDurationSeconds())
                .textContent(request.getTextContent())
                .preview(request.isPreview())
                .build();

        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonResponse update(Long lessonId, LessonRequest request, AuthenticatedUser currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
        assertOwnerOrAdmin(lesson.getSection().getCourse(), currentUser);

        lesson.setTitle(request.getTitle());
        lesson.setPosition(request.getPosition());
        lesson.setContentType(LessonContentType.valueOf(request.getContentType().toUpperCase()));
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDurationSeconds(request.getDurationSeconds());
        lesson.setTextContent(request.getTextContent());
        lesson.setPreview(request.isPreview());

        return lessonMapper.toResponse(lesson);
    }

    @Override
    @Transactional
    public void delete(Long lessonId, AuthenticatedUser currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
        assertOwnerOrAdmin(lesson.getSection().getCourse(), currentUser);
        lessonRepository.delete(lesson);
    }

    private void assertOwnerOrAdmin(Course course, AuthenticatedUser currentUser) {
        boolean isOwner = course.isOwnedBy(currentUser.userId());
        boolean isAdmin = currentUser.hasRole(ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("You do not have permission to modify this course's lessons");
        }
    }
}
