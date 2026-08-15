package com.elearning.course.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseFileResponse {
    private Long id;
    private Long lessonId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long sizeBytes;
    private LocalDateTime uploadedAt;
}
