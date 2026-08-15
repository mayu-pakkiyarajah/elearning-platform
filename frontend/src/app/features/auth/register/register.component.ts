import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiErrorResponse } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { AlertComponent } from '../../../shared/components/alert/alert.component';

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AlertComponent],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  form = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(PASSWORD_PATTERN)]],
    requestedRole: ['STUDENT', [Validators.required]],
  });

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    this.authService.register(this.form.getRawValue() as any).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        // Registration doesn't auto-login by design (backend just creates the account) —
        // send them to login with a hint rather than silently signing them in.
        this.router.navigate(['/auth/login'], { queryParams: { registered: '1' } });
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const apiError = err.error as ApiErrorResponse | undefined;
        this.errorMessage.set(apiError?.message ?? 'Something went wrong. Please try again.');
      },
    });
  }
}
