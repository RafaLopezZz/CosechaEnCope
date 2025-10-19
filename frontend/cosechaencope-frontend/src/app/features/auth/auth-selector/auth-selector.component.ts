import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../../home/navbar/navbar.component';
import { FooterComponent } from '../../home/footer/footer.component';

@Component({
  standalone: true,
  selector: 'app-auth-selector',
  imports: [CommonModule, RouterModule, NavbarComponent, FooterComponent],
  templateUrl: './auth-selector.component.html',
  styleUrls: ['./auth-selector.component.scss'],
})
export class AuthSelectorComponent {
}