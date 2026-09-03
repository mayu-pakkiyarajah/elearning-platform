import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CertificateVerification } from '../../../core/models/certificate.model';
import { CertificateService } from '../../../core/services/certificate.service';

@Component({
  selector: 'app-certificate-verify',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './certificate-verify.component.html',
})
export class CertificateVerifyComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private certificateService = inject(CertificateService);

  readonly result = signal<CertificateVerification | null>(null);
  readonly isLoading = signal(true);
  readonly notFound = signal(false);
  readonly pdfUrl = signal<string | null>(null);

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('code');
    if (!code) {
      this.notFound.set(true);
      this.isLoading.set(false);
      return;
    }

    this.pdfUrl.set(this.certificateService.publicPdfUrl(code));

    this.certificateService.verify(code).subscribe({
      next: (result) => {
        this.result.set(result);
        this.isLoading.set(false);
      },
      error: () => {
        this.notFound.set(true);
        this.isLoading.set(false);
      },
    });
  }
}
