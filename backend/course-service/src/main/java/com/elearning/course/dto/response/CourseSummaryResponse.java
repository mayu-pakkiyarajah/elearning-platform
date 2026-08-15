package com.elearning.course.dto.response;

import lombok.*;

import java.math.BigDecimal;

/** Lightweight view for catalog/search listing pages — no sections/lessons payload. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSummaryResponse {
    private Long id;
    private String title;
    private String slug;
    private String subtitle;
    private CategoryResponse category;
    private String level;
    private String language;
    private BigDecimal price;
    private String thumbnailUrl;
    private String status;
    private Long instructorId;
}
