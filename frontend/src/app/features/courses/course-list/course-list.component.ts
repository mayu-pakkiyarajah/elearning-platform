import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { Category } from '../../../core/models/category.model';
import { CourseSummary } from '../../../core/models/course.model';
import { PageResponse } from '../../../core/models/page.model';
import { CategoryService } from '../../../core/services/category.service';
import { CourseService } from '../../../core/services/course.service';

const PAGE_SIZE = 12;

@Component({
  selector: 'app-course-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './course-list.component.html',
  styleUrl: './course-list.component.scss',
})
export class CourseListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private courseService = inject(CourseService);
  private categoryService = inject(CategoryService);

  readonly categories = signal<Category[]>([]);
  readonly page = signal<PageResponse<CourseSummary> | null>(null);
  readonly isLoading = signal(true);
  readonly currentPage = signal(0);

  filters = this.fb.group({
    search: [''],
    categoryId: [''],
    level: [''],
  });

  ngOnInit(): void {
    this.categoryService.listAll().subscribe((categories) => this.categories.set(categories));

    this.filters.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)))
      .subscribe(() => {
        this.currentPage.set(0);
        this.load();
      });

    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    const { search, categoryId, level } = this.filters.getRawValue();

    this.courseService
      .browse({
        search: search || null,
        categoryId: categoryId ? Number(categoryId) : null,
        level: level || null,
        page: this.currentPage(),
        size: PAGE_SIZE,
      })
      .subscribe({
        next: (result) => {
          this.page.set(result);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
  }

  goToPage(pageNumber: number): void {
    this.currentPage.set(pageNumber);
    this.load();
  }

  clearFilters(): void {
    this.filters.reset({ search: '', categoryId: '', level: '' });
  }
}
