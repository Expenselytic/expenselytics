import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef } from '@angular/core';
import { AuthService } from './auth.service';
import { safeReturnUrl } from './auth.guard';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  mode: 'login' | 'signup' =
    this.route.snapshot.queryParamMap.get('mode') === 'signup'
      ? 'signup'
      : 'login';
  name = '';
  email = '';
  password = '';
  busy = false;
  error = '';

  submit(): void {
    if (this.busy) return;
    this.busy = true;
    this.error = '';
    this.auth
      .authenticate(this.mode, this.name, this.email, this.password)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.password = '';
          this.busy = false;
          void this.router.navigateByUrl(
            safeReturnUrl(
              this.route.snapshot.queryParamMap.get('returnUrl'),
            ),
            { replaceUrl: true },
          );
        },
        error: (error) => {
          this.busy = false;
          this.error =
            error.status === 401
              ? 'Invalid email or password.'
              : error.status === 409
                ? 'An account with this email already exists.'
                : error.status === 400
                  ? 'Check your details and password length.'
                  : 'Could not connect. Please try again.';
        },
      });
  }

  switchMode(): void {
    this.mode = this.mode === 'login' ? 'signup' : 'login';
    this.error = '';
    this.password = '';
  }
}
