import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { JwtResponse } from '../../shared/models/auth.models';

@Injectable({ providedIn: 'root' })
export class UserStoreService {
  private _user$ = new BehaviorSubject<JwtResponse | null>(null);
  user$ = this._user$.asObservable();
  set(user: JwtResponse) { this._user$.next(user); }
  clear() { this._user$.next(null); }
  snapshot() { return this._user$.value; }
}