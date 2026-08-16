import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Course, CourseRequest, CourseSummary } from '../models/course.model';
import { PageResponse } from '../models/page.model';

export interface CourseBrowseFilters {
  categoryId?: number | null;
  level?: string | null;
  language?: string | null;
  search?: string | null;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class CourseService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.courseApiUrl}/courses`;

  browse(filters: CourseBrowseFilters): Observable<PageResponse<CourseSummary>> {
    let params = new HttpParams()
      .set('page', filters.page ?? 0)
      .set('size', filters.size ?? 12);

    if (filters.categoryId) params = params.set('categoryId', filters.categoryId);
    if (filters.level) params = params.set('level', filters.level);
    if (filters.language) params = params.set('language', filters.language);
    if (filters.search) params = params.set('search', filters.search);

    return this.http.get<PageResponse<CourseSummary>>(this.baseUrl, { params });
  }

  getBySlug(slug: string): Observable<Course> {
    return this.http.get<Course>(`${this.baseUrl}/${slug}`);
  }

  listMine(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.baseUrl}/mine`);
  }

  getMineById(id: number): Observable<Course | undefined> {
    // course-service doesn't expose GET /courses/{id} for instructors (only by slug,
    // which is public-catalog-oriented) — reuse listMine() and find locally. Fine at
    // this scale; worth a dedicated endpoint if instructors get hundreds of courses.
    return new Observable((subscriber) => {
      this.listMine().subscribe({
        next: (courses) => {
          subscriber.next(courses.find((c) => c.id === id));
          subscriber.complete();
        },
        error: (err) => subscriber.error(err),
      });
    });
  }

  create(request: CourseRequest): Observable<Course> {
    return this.http.post<Course>(this.baseUrl, request);
  }

  update(id: number, request: CourseRequest): Observable<Course> {
    return this.http.put<Course>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  publish(id: number): Observable<Course> {
    return this.http.patch<Course>(`${this.baseUrl}/${id}/publish`, {});
  }

  unpublish(id: number): Observable<Course> {
    return this.http.patch<Course>(`${this.baseUrl}/${id}/unpublish`, {});
  }
}
