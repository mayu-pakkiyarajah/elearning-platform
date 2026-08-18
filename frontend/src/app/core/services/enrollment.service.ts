import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EnrolledStudent, Enrollment, EnrollmentDetail } from '../models/enrollment.model';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.enrollmentApiUrl}/enrollments`;

  /** Idempotent on the backend — calling this when already enrolled just returns the existing record. */
  enroll(courseId: number): Observable<Enrollment> {
    return this.http.post<Enrollment>(this.baseUrl, { courseId });
  }

  listMine(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.baseUrl}/mine`);
  }

  /** 404s if the current user isn't enrolled in this course — callers should catch that, not treat it as an error. */
  getDetail(courseId: number): Observable<EnrollmentDetail> {
    return this.http.get<EnrollmentDetail>(`${this.baseUrl}/mine/${courseId}`);
  }

  markLessonComplete(courseId: number, lessonId: number): Observable<EnrollmentDetail> {
    return this.http.post<EnrollmentDetail>(`${this.baseUrl}/mine/${courseId}/lessons/${lessonId}/complete`, {});
  }

  markLessonIncomplete(courseId: number, lessonId: number): Observable<EnrollmentDetail> {
    return this.http.delete<EnrollmentDetail>(`${this.baseUrl}/mine/${courseId}/lessons/${lessonId}/complete`);
  }

  listStudentsForCourse(courseId: number): Observable<EnrolledStudent[]> {
    return this.http.get<EnrolledStudent[]>(`${this.baseUrl}/course/${courseId}`);
  }
}
