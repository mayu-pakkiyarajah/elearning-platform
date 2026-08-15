package com.elearning.course.service.impl;

import com.elearning.course.dto.request.CourseRequest;
import com.elearning.course.dto.response.CourseResponse;
import com.elearning.course.dto.response.CourseSummaryResponse;
import com.elearning.course.dto.response.PageResponse;
import com.elearning.course.entity.Category;
import com.elearning.course.entity.Course;
import com.elearning.course.entity.CourseLevel;
import com.elearning.course.entity.CourseStatus;
import com.elearning.course.exception.DuplicateResourceException;
import com.elearning.course.exception.ForbiddenOperationException;
import com.elearning.course.exception.ResourceNotFoundException;
import com.elearning.course.mapper.CourseMapper;
import com.elearning.course.repository.CategoryRepository;
import com.elearning.course.repository.CourseRepository;
import com.elearning.course.security.AuthenticatedUser;
import com.elearning.course.service.CourseService;
import com.elearning.course.service.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> browse(
            Long categoryId, String level, String language, String search, Pageable pageable) {

        CourseLevel levelEnum = level != null ? CourseLevel.valueOf(level.toUpperCase()) : null;

        Page<Course> page = courseRepository.search(
                CourseStatus.PUBLISHED, categoryId, levelEnum, language, search, pageable);

        return PageResponse.from(page.map(courseMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getBySlug(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + slug));
        return courseMapper.toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> listMine(AuthenticatedUser currentUser) {
        return courseRepository.findByInstructorId(currentUser.userId()).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CourseResponse create(CourseRequest request, AuthenticatedUser currentUser) {
        Category category = resolveCategory(request.getCategoryId());

        String slug = SlugUtil.uniqueSlugify(request.getTitle(), courseRepository::existsBySlug);

        Course course = Course.builder()
                .instructorId(currentUser.userId())
                .category(category)
                .title(request.getTitle())
                .slug(slug)
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .level(CourseLevel.valueOf(request.getLevel().toUpperCase()))
                .language(request.getLanguage())
                .price(request.getPrice())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(CourseStatus.DRAFT)
                .build();

        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse update(Long courseId, CourseRequest request, AuthenticatedUser currentUser) {
        Course course = getOwnedCourseOrThrow(courseId, currentUser);
        Category category = resolveCategory(request.getCategoryId());

        // slug is intentionally NOT regenerated on title edits — a published course's
        // URL should stay stable for anyone who's already bookmarked/shared it.
        course.setTitle(request.getTitle());
        course.setSubtitle(request.getSubtitle());
        course.setDescription(request.getDescription());
        course.setCategory(category);
        course.setLevel(CourseLevel.valueOf(request.getLevel().toUpperCase()));
        course.setLanguage(request.getLanguage());
        course.setPrice(request.getPrice());
        course.setThumbnailUrl(request.getThumbnailUrl());

        return courseMapper.toResponse(course);
    }

    @Override
    @Transactional
    public void delete(Long courseId, AuthenticatedUser currentUser) {
        Course course = getOwnedCourseOrThrow(courseId, currentUser);
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public CourseResponse setPublished(Long courseId, boolean published, AuthenticatedUser currentUser) {
        Course course = getOwnedCourseOrThrow(courseId, currentUser);
        // TODO once instructorApproved is included in the JWT claims: block publishing
        // for instructors who haven't been approved by an admin yet.
        course.setStatus(published ? CourseStatus.PUBLISHED : CourseStatus.DRAFT);
        return courseMapper.toResponse(course);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    /** Fetches a course and enforces "must be the owning instructor, or an admin". */
    private Course getOwnedCourseOrThrow(Long courseId, AuthenticatedUser currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        boolean isOwner = course.isOwnedBy(currentUser.userId());
        boolean isAdmin = currentUser.hasRole(ROLE_ADMIN);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("You do not have permission to modify this course");
        }
        return course;
    }
}
