import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (message) {
      <div class="alert" [class.alert-danger]="type === 'error'" [class.alert-success]="type === 'success'" role="alert">
        {{ message }}
      </div>
    }
  `,
})
export class AlertComponent {
  @Input() type: 'error' | 'success' = 'error';
  @Input() message: string | null = null;
}
