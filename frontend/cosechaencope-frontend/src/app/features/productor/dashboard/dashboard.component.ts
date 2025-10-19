import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { UserStoreService } from '../../../core/services/user-store.service';
import { ProductorService } from '../../../core/services/productor.service';
import { AuthService } from '../../../core/services/auth.service';
import { ProductorResponse } from '../../../shared/models/productor.models';

@Component({
  selector: 'app-dashboard-productor',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardProductorComponent implements OnInit {
  private userStore = inject(UserStoreService);
  private productorService = inject(ProductorService);
  private authService = inject(AuthService);
  private router = inject(Router);

  productor: ProductorResponse | null = null;
  estadisticas: any = null;
  loading = true;
  sidebarCollapsed = false;

  currentUser = this.userStore.snapshot();

  ngOnInit() {
    if (!this.currentUser || this.currentUser.tipoUsuario !== 'PRODUCTOR') {
      this.router.navigateByUrl('/login/productores');
      return;
    }

    this.loadProductorData();
  }

  loadProductorData() {
    if (!this.currentUser) return;

    this.productorService.getProductorPorUsuario(this.currentUser.idUsuario).subscribe({
      next: (productor) => {
        this.productor = productor;
        this.loadEstadisticas();
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadEstadisticas() {
    if (!this.productor) return;

    this.productorService.getEstadisticasProductor(this.productor.idProductor).subscribe({
      next: (stats) => {
        this.estadisticas = stats;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout() {
    this.authService.logout();
  }
}