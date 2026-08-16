import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Section, SectionRequest } from '../models/course.model';

@Injectable({ providedIn: 'root' })
export class SectionService {
  private http = inject(HttpClient);
  private baseUrl = environment.courseApiUrl;

  create(courseId: number, request: SectionRequest): Observable<Section> {
    return this.http.post<Section>(`${this.baseUrl}/courses/${courseId}/sections`, request);
  }

  update(sectionId: number, request: SectionRequest): Observable<Section> {
    return this.http.put<Section>(`${this.baseUrl}/sections/${sectionId}`, request);
  }

  delete(sectionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/sections/${sectionId}`);
  }
}
