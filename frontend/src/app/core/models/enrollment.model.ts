export type EnrollmentStatus = 'ACTIVE' | 'COMPLETED';

export interface Enrollment {
  id: number;
  courseId: number;
  courseTitle: string;
  courseSlug: string;
  status: EnrollmentStatus;
  totalLessons: number;
  completedLessons: number;
  progressPercent: number;
  enrolledAt: string;
  completedAt?: string;
}

export interface EnrollmentDetail {
  enrollment: Enrollment;
  completedLessonIds: number[];
}

export interface EnrolledStudent {
  studentId: number;
  status: EnrollmentStatus;
  progressPercent: number;
  enrolledAt: string;
}
