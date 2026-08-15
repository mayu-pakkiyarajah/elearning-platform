package com.elearning.course.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionResponse {
    private Long id;
    private String title;
    private Integer position;
    private List<LessonResponse> lessons;
}
