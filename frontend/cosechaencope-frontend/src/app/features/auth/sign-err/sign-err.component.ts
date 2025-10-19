import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../../home/navbar/navbar.component';
import { FooterComponent } from '../../home/footer/footer.component';

@Component({
  standalone: true,
  selector: 'app-sign-err',
  imports: [CommonModule, RouterModule, NavbarComponent, FooterComponent],
  template: `
  <app-navbar></app-navbar>
  <section class="error container">
    <h1>Ha ocurrido un problema</h1>
    <p>Código: {{code}}</p>
    <p>{{message}}</p>
    <div class="actions">
      <a routerLink="/">Ir a inicio</a>
      <a routerLink="/login">Ir a login</a>
    </div>
  </section>
  <app-footer></app-footer>
  `,
  styles: [`.container{max-width:560px;margin:2rem auto}.actions{display:flex;gap:12px}`]
})
export class SignErrComponent {
  private route = inject(ActivatedRoute);
  code = this.route.snapshot.queryParamMap.get('code') ?? '0';
  message = this.route.snapshot.queryParamMap.get('m') ?? 'Intenta nuevamente en unos minutos';
}
