import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiErrorResponse } from '../../../core/models/auth.model';
import { Course, LessonRequest, Section, SectionRequest } from '../../../core/models/course.model';
import { CourseService } from '../../../core/services/course.service';
import { LessonService } from '../../../core/services/lesson.service';
import { SectionService } from '../../../core/services/section.service';
import { AlertComponent } from '../../../shared/components/alert/alert.component';

@Component({
  selector: 'app-course-curriculum',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AlertComponent],
  templateUrl: './course-curriculum.component.html',
})
export class CourseCurriculumComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private courseService = inject(CourseService);
  private sectionService = inject(SectionService);
  private lessonService = inject(LessonService);

  readonly course = signal<Course | null>(null);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly addingSection = signal(false);
  readonly addingLessonForSection = signal<number | null>(null);
  readonly editingSectionId = signal<number | null>(null);

  private courseId!: number;

  sectionForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
  });

  lessonForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    contentType: ['VIDEO', [Validators.required]],
    videoUrl: [''],
    durationSeconds: [null as number | null],
    textContent: [''],
    preview: [false],
  });

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.courseService.getMineById(this.courseId).subscribe({
      next: (course) => {
        this.isLoading.set(false);
        if (!course) {
          this.errorMessage.set('Course not found, or you do not own it.');
          return;
        }
        this.course.set(course);
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Could not load this course.');
      },
    });
  }

  // ---- Sections ----

  startAddSection(): void {
    this.sectionForm.reset({ title: '' });
    this.addingSection.set(true);
  }

  submitNewSection(): void {
    if (this.sectionForm.invalid) {
      this.sectionForm.markAllAsTouched();
      return;
    }
    const position = (this.course()?.sections.length ?? 0) + 1;
    const request: SectionRequest = { title: this.sectionForm.value.title!, position };

    this.sectionService.create(this.courseId, request).subscribe({
      next: () => {
        this.addingSection.set(false);
        this.load();
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  startEditSection(section: Section): void {
    this.sectionForm.reset({ title: section.title });
    this.editingSectionId.set(section.id);
  }

  submitEditSection(section: Section): void {
    if (this.sectionForm.invalid) {
      this.sectionForm.markAllAsTouched();
      return;
    }
    const request: SectionRequest = { title: this.sectionForm.value.title!, position: section.position };

    this.sectionService.update(section.id, request).subscribe({
      next: () => {
        this.editingSectionId.set(null);
        this.load();
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  deleteSection(section: Section): void {
    if (!confirm(`Delete section "${section.title}" and all its lessons?`)) {
      return;
    }
    this.sectionService.delete(section.id).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  // ---- Lessons ----

  startAddLesson(sectionId: number): void {
    this.lessonForm.reset({
      title: '', contentType: 'VIDEO', videoUrl: '', durationSeconds: null, textContent: '', preview: false,
    });
    this.addingLessonForSection.set(sectionId);
  }

  submitNewLesson(section: Section): void {
    if (this.lessonForm.invalid) {
      this.lessonForm.markAllAsTouched();
      return;
    }
    const raw = this.lessonForm.getRawValue();
    const request: LessonRequest = {
      title: raw.title!,
      position: section.lessons.length + 1,
      contentType: raw.contentType as LessonRequest['contentType'],
      videoUrl: raw.videoUrl || undefined,
      durationSeconds: raw.durationSeconds ?? undefined,
      textContent: raw.textContent || undefined,
      preview: raw.preview!,
    };

    this.lessonService.create(section.id, request).subscribe({
      next: () => {
        this.addingLessonForSection.set(null);
        this.load();
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  deleteLesson(lessonId: number): void {
    if (!confirm('Delete this lesson?')) {
      return;
    }
    this.lessonService.delete(lessonId).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  cancelAdd(): void {
    this.addingSection.set(false);
    this.addingLessonForSection.set(null);
    this.editingSectionId.set(null);
  }

  private showError(err: HttpErrorResponse): void {
    const apiError = err.error as ApiErrorResponse | undefined;
    this.errorMessage.set(apiError?.message ?? 'Something went wrong. Please try again.');
  }
}
