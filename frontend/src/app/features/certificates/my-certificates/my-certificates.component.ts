import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Certificate } from '../../../core/models/certificate.model';
import { CertificateService } from '../../../core/services/certificate.service';

@Component({
  selector: 'app-my-certificates',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-certificates.component.html',
})
export class MyCertificatesComponent implements OnInit {
  private certificateService = inject(CertificateService);

  readonly certificates = signal<Certificate[]>([]);
  readonly isLoading = signal(true);
  readonly downloadingId = signal<number | null>(null);
  readonly copiedCode = signal<string | null>(null);

  ngOnInit(): void {
    this.certificateService.listMine().subscribe({
      next: (certs) => {
        this.certificates.set(certs);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  download(cert: Certificate): void {
    this.downloadingId.set(cert.id);
    this.certificateService.downloadMine(cert.id).subscribe({
      next: (blob) => {
        this.downloadingId.set(null);
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `certificate-${cert.verificationCode}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.downloadingId.set(null),
    });
  }

  copyShareLink(cert: Certificate): void {
    navigator.clipboard.writeText(cert.verificationUrl).then(() => {
      this.copiedCode.set(cert.verificationCode);
      setTimeout(() => this.copiedCode.set(null), 2000);
    });
  }
}
