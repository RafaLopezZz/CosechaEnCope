import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { inject } from '@angular/core';
import { UserStoreService } from '../services/user-store.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const router = inject(Router);
  const store = inject(UserStoreService);
  const currentUser = store.snapshot();

  if (!currentUser) {
    router.navigateByUrl('/auth');
    return false;
  }

  const rolesNeed = route.data['roles'] as string[] | undefined;
  const tipoUsuarioNeed = route.data['tipoUsuario'] as string | undefined;
  
  // Verificar tipo de usuario si está especificado
  if (tipoUsuarioNeed && currentUser.tipoUsuario !== tipoUsuarioNeed) {
    const redirectUrl = currentUser.tipoUsuario === 'PRODUCTOR' 
      ? '/productor/dashboard' 
      : '/articulos';
    router.navigateByUrl(redirectUrl);
    return false;
  }

  // Verificar roles si están especificados
  const rolesHave = currentUser.roles ?? [];
  const ok = !rolesNeed || rolesNeed.some(r => rolesHave.includes(r));
  if (!ok) {
    router.navigate(['/error'], { queryParams: { code: 403, m: 'Sin permisos suficientes' } });
  }
  return ok;
};
