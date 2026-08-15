import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AlertComponent } from '../../../shared/components/alert/alert.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AlertComponent],
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  readonly isSubmitting = signal(false);
  readonly submitted = signal(false);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    // Backend always returns 202 regardless of whether the email exists,
    // so we always show the same confirmation — this endpoint never errors.
    this.authService.forgotPassword(this.form.getRawValue().email as string).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.submitted.set(true);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.submitted.set(true);
      },
    });
  }
}
