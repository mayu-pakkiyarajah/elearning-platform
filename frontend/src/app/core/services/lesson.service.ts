import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Lesson, LessonRequest } from '../models/course.model';

@Injectable({ providedIn: 'root' })
export class LessonService {
  private http = inject(HttpClient);
  private baseUrl = environment.courseApiUrl;

  create(sectionId: number, request: LessonRequest): Observable<Lesson> {
    return this.http.post<Lesson>(`${this.baseUrl}/sections/${sectionId}/lessons`, request);
  }

  update(lessonId: number, request: LessonRequest): Observable<Lesson> {
    return this.http.put<Lesson>(`${this.baseUrl}/lessons/${lessonId}`, request);
  }

  delete(lessonId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/lessons/${lessonId}`);
  }
}
