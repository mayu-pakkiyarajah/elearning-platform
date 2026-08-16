import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiErrorResponse } from '../../../core/models/auth.model';
import { Category } from '../../../core/models/category.model';
import { CourseRequest } from '../../../core/models/course.model';
import { CategoryService } from '../../../core/services/category.service';
import { CourseService } from '../../../core/services/course.service';
import { AlertComponent } from '../../../shared/components/alert/alert.component';

@Component({
  selector: 'app-course-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AlertComponent],
  templateUrl: './course-form.component.html',
})
export class CourseFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private courseService = inject(CourseService);
  private categoryService = inject(CategoryService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly categories = signal<Category[]>([]);
  readonly isEditMode = signal(false);
  readonly isSubmitting = signal(false);
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  private courseId: number | null = null;

  form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    subtitle: ['', [Validators.maxLength(300)]],
    description: [''],
    categoryId: [''],
    level: ['BEGINNER', [Validators.required]],
    language: ['English', [Validators.required]],
    price: [0, [Validators.required, Validators.min(0)]],
    thumbnailUrl: [''],
  });

  ngOnInit(): void {
    this.categoryService.listAll().subscribe((categories) => this.categories.set(categories));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode.set(true);
      this.courseId = Number(idParam);
      this.isLoading.set(true);

      this.courseService.getMineById(this.courseId).subscribe({
        next: (course) => {
          this.isLoading.set(false);
          if (!course) {
            this.errorMessage.set('Course not found, or you do not own it.');
            return;
          }
          this.form.patchValue({
            title: course.title,
            subtitle: course.subtitle ?? '',
            description: course.description ?? '',
            categoryId: course.category?.id ? String(course.category.id) : '',
            level: course.level,
            language: course.language,
            price: course.price,
            thumbnailUrl: course.thumbnailUrl ?? '',
          });
        },
        error: () => {
          this.isLoading.set(false);
          this.errorMessage.set('Could not load this course.');
        },
      });
    }
  }

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    const raw = this.form.getRawValue();
    const request: CourseRequest = {
      title: raw.title!,
      subtitle: raw.subtitle || undefined,
      description: raw.description || undefined,
      categoryId: raw.categoryId ? Number(raw.categoryId) : null,
      level: raw.level as CourseRequest['level'],
      language: raw.language!,
      price: Number(raw.price),
      thumbnailUrl: raw.thumbnailUrl || undefined,
    };

    const action = this.isEditMode() && this.courseId
      ? this.courseService.update(this.courseId, request)
      : this.courseService.create(request);

    action.subscribe({
      next: (course) => {
        this.isSubmitting.set(false);
        this.router.navigate(['/instructor/courses', course.id, 'curriculum']);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const apiError = err.error as ApiErrorResponse | undefined;
        this.errorMessage.set(apiError?.message ?? 'Something went wrong. Please try again.');
      },
    });
  }
}
