import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Course } from '../../../core/models/course.model';
import { CourseService } from '../../../core/services/course.service';

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-courses.component.html',
})
export class MyCoursesComponent implements OnInit {
  private courseService = inject(CourseService);

  readonly courses = signal<Course[]>([]);
  readonly isLoading = signal(true);
  readonly actioningId = signal<number | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.courseService.listMine().subscribe({
      next: (courses) => {
        this.courses.set(courses);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  togglePublish(course: Course): void {
    this.actioningId.set(course.id);
    const action = course.status === 'PUBLISHED'
      ? this.courseService.unpublish(course.id)
      : this.courseService.publish(course.id);

    action.subscribe({
      next: (updated) => {
        this.courses.update((list) => list.map((c) => (c.id === updated.id ? updated : c)));
        this.actioningId.set(null);
      },
      error: () => this.actioningId.set(null),
    });
  }

  deleteCourse(course: Course): void {
    if (!confirm(`Delete "${course.title}"? This can't be undone.`)) {
      return;
    }
    this.actioningId.set(course.id);
    this.courseService.delete(course.id).subscribe({
      next: () => {
        this.courses.update((list) => list.filter((c) => c.id !== course.id));
        this.actioningId.set(null);
      },
      error: () => this.actioningId.set(null),
    });
  }
}
