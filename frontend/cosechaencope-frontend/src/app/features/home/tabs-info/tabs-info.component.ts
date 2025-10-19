// src/app/features/home/tabs-info/tabs-info.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-tabs-info',
  imports: [CommonModule],
  templateUrl: './tabs-info.component.html',
  styleUrls: ['./tabs-info.component.scss'],
})
export class TabsInfoComponent {
  tab = 0; // pestaña activa por defecto

  setTab(i: number) { this.tab = i; }

  isSelected(i: number) { return this.tab === i ? 'true' : 'false'; } // útil para [attr.aria-selected]
}
