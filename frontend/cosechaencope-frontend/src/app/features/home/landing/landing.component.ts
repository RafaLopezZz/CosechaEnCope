// src/app/features/home/landing/landing.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../navbar/navbar.component';
import { HeroComponent } from '../hero/hero.component';
import { FeaturesSectionComponent } from '../features-section/features-section.component';
import { GalleryCarouselComponent } from '../gallery-carousel/gallery-carousel.component';
import { TabsInfoComponent } from '../tabs-info/tabs-info.component';
import { TestimonialSectionComponent } from '../testimonial-section/testimonial-section.component';
import { CtaSectionComponent } from '../cta-section/cta-section.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  standalone: true,
  selector: 'app-landing',
  imports: [
    CommonModule,
    NavbarComponent,
    HeroComponent,
    FeaturesSectionComponent,
    GalleryCarouselComponent,
    TabsInfoComponent,
    TestimonialSectionComponent,
    CtaSectionComponent,
    FooterComponent
  ],
  template: `
    <app-navbar/>
    <app-hero/>
    <app-features-section/>
    <app-gallery-carousel/>
    <app-tabs-info/>
    <app-testimonial-section/>
    <app-cta-section/>
    <app-footer/>
  `
})
export class LandingComponent {}

