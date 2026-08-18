import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Course } from '../../../core/models/course.model';
import { Enrollment } from '../../../core/models/enrollment.model';
import { AuthService } from '../../../core/services/auth.service';
import { CourseService } from '../../../core/services/course.service';
import { EnrollmentService } from '../../../core/services/enrollment.service';

type EnrollmentState = 'CHECKING' | 'NOT_ENROLLED' | 'ENROLLED' | 'NOT_APPLICABLE';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.scss',
})
export class CourseDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private courseService = inject(CourseService);
  private enrollmentService = inject(EnrollmentService);
  authService = inject(AuthService);

  readonly course = signal<Course | null>(null);
  readonly isLoading = signal(true);
  readonly notFound = signal(false);
  readonly expandedSectionId = signal<number | null>(null);

  readonly enrollmentState = signal<EnrollmentState>('CHECKING');
  readonly enrollment = signal<Enrollment | null>(null);
  readonly isEnrolling = signal(false);
  readonly enrollError = signal<string | null>(null);

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.notFound.set(true);
      this.isLoading.set(false);
      return;
    }

    this.courseService.getBySlug(slug).subscribe({
      next: (course) => {
        this.course.set(course);
        this.isLoading.set(false);
        if (course.sections.length > 0) {
          this.expandedSectionId.set(course.sections[0].id);
        }
        this.checkEnrollment(course.id);
      },
      error: () => {
        this.notFound.set(true);
        this.isLoading.set(false);
      },
    });
  }

  private checkEnrollment(courseId: number): void {
    // Only students can enroll — instructors/admins (or anonymous visitors) just browse.
    if (!this.authService.isLoggedIn() || !this.authService.hasRole('ROLE_STUDENT')) {
      this.enrollmentState.set('NOT_APPLICABLE');
      return;
    }

    this.enrollmentService.getDetail(courseId).subscribe({
      next: (detail) => {
        this.enrollment.set(detail.enrollment);
        this.enrollmentState.set('ENROLLED');
      },
      error: (err: HttpErrorResponse) => {
        // a 404 here just means "not enrolled yet" — that's expected, not a failure
        if (err.status === 404) {
          this.enrollmentState.set('NOT_ENROLLED');
        } else {
          this.enrollmentState.set('NOT_APPLICABLE');
        }
      },
    });
  }

  enrollNow(): void {
    const course = this.course();
    if (!course) return;

    this.isEnrolling.set(true);
    this.enrollError.set(null);

    this.enrollmentService.enroll(course.id).subscribe({
      next: (enrollment) => {
        this.isEnrolling.set(false);
        this.enrollment.set(enrollment);
        this.enrollmentState.set('ENROLLED');
        this.router.navigate(['/learn', course.slug]);
      },
      error: () => {
        this.isEnrolling.set(false);
        this.enrollError.set('Could not enroll right now — please try again.');
      },
    });
  }

  toggleSection(sectionId: number): void {
    this.expandedSectionId.set(this.expandedSectionId() === sectionId ? null : sectionId);
  }

  totalLessons(course: Course): number {
    return course.sections.reduce((sum, s) => sum + s.lessons.length, 0);
  }

  formatDuration(seconds?: number): string {
    if (!seconds) return '';
    const minutes = Math.round(seconds / 60);
    return `${minutes} min`;
  }
}
