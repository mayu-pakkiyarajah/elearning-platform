import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Certificate, CertificateVerification } from '../models/certificate.model';

@Injectable({ providedIn: 'root' })
export class CertificateService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.certificateApiUrl}/certificates`;

  /** Idempotent on the backend — calling this for a course you already have a certificate for just returns it. */
  generate(courseId: number): Observable<Certificate> {
    return this.http.post<Certificate>(this.baseUrl, { courseId });
  }

  listMine(): Observable<Certificate[]> {
    return this.http.get<Certificate[]>(`${this.baseUrl}/mine`);
  }

  /** Requires auth — fetched as a blob so the interceptor's Bearer token is actually sent. */
  downloadMine(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/download`, { responseType: 'blob' });
  }

  /** Public — no auth needed, safe to link to directly. */
  verify(code: string): Observable<CertificateVerification> {
    return this.http.get<CertificateVerification>(`${this.baseUrl}/verify/${code}`);
  }

  publicPdfUrl(code: string): string {
    return `${this.baseUrl}/verify/${code}/pdf`;
  }
}
