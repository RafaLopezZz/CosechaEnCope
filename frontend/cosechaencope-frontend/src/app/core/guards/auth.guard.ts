import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = sessionStorage.getItem('authToken');
  if (!token) {
    router.navigateByUrl('/auth');
    return false;
  }
  return true;
};
