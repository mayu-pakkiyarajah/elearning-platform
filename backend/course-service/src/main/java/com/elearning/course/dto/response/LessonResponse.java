package com.elearning.course.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponse {
    private Long id;
    private String title;
    private Integer position;
    private String contentType;
    private String videoUrl;
    private Integer durationSeconds;
    private String textContent;
    private boolean preview;
}
