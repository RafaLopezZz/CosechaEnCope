import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginRequest, JwtResponse } from '../../shared/models/auth.models';
import { UsuarioRequest, UsuarioResponse } from '../../shared/models/usuario.models';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { UserStoreService } from './user-store.service';
import { API_ENDPOINTS } from '../config';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private store = inject(UserStoreService);
  private router = inject(Router);

  login(dto: LoginRequest) {
    return this.http.post<JwtResponse>(API_ENDPOINTS.AUTH.LOGIN, dto).pipe(
      tap(res => {
        sessionStorage.setItem('authToken', res.token);
        this.store.set(res);
      })
    );
  }

  loginProductor(dto: LoginRequest) {
    return this.http.post<JwtResponse>(API_ENDPOINTS.AUTH.LOGIN_PRODUCTORES, dto).pipe(
      tap(res => {
        sessionStorage.setItem('authToken', res.token);
        this.store.set(res);
      })
    );
  }

  registerCliente(dto: UsuarioRequest) {
    return this.http.post<UsuarioResponse>(API_ENDPOINTS.AUTH.REGISTRO_CLIENTES, dto);
  }

  registerProductor(dto: UsuarioRequest) {
    return this.http.post<UsuarioResponse>(API_ENDPOINTS.AUTH.REGISTRO_PRODUCTORES, dto);
  }

  logout() {
    sessionStorage.removeItem('authToken');
    this.store.clear();
    this.router.navigateByUrl('/login');
  }
}
