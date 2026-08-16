import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Course } from '../../../core/models/course.model';
import { CourseService } from '../../../core/services/course.service';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.scss',
})
export class CourseDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private courseService = inject(CourseService);

  readonly course = signal<Course | null>(null);
  readonly isLoading = signal(true);
  readonly notFound = signal(false);
  readonly expandedSectionId = signal<number | null>(null);

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
      },
      error: () => {
        this.notFound.set(true);
        this.isLoading.set(false);
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
