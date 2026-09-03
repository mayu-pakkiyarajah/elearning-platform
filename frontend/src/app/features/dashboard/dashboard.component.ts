import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Enrollment } from '../../core/models/enrollment.model';
import { AuthService } from '../../core/services/auth.service';
import { CertificateService } from '../../core/services/certificate.service';
import { EnrollmentService } from '../../core/services/enrollment.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private enrollmentService = inject(EnrollmentService);
  private certificateService = inject(CertificateService);
  private router = inject(Router);
  authService = inject(AuthService);

  readonly enrollments = signal<Enrollment[]>([]);
  readonly isLoading = signal(false);
  readonly generatingCertFor = signal<number | null>(null);

  ngOnInit(): void {
    if (this.authService.hasRole('ROLE_STUDENT')) {
      this.isLoading.set(true);
      this.enrollmentService.listMine().subscribe({
        next: (enrollments) => {
          this.enrollments.set(enrollments);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
    }
  }

  getCertificate(enrollment: Enrollment): void {
    this.generatingCertFor.set(enrollment.courseId);
    this.certificateService.generate(enrollment.courseId).subscribe({
      next: () => {
        this.generatingCertFor.set(null);
        this.router.navigate(['/certificates']);
      },
      error: () => this.generatingCertFor.set(null),
    });
  }
}
