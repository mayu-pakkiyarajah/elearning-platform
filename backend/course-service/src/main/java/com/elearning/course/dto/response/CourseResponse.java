package com.elearning.course.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Full detail view — course + sections + lessons. Used for the course detail page. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private Long instructorId;
    private CategoryResponse category;
    private String title;
    private String slug;
    private String subtitle;
    private String description;
    private String level;
    private String language;
    private BigDecimal price;
    private String thumbnailUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SectionResponse> sections;
}
