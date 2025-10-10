/**
 * Cosecha en Cope - Main JavaScript for SSR Landing Pages
 * Optimized for performance and progressive enhancement
 */

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize all components
    initSmoothScrolling();
    initScrollAnimations();
    initNavbarBehavior();
    initLazyLoading();
    initPerformanceOptimizations();
    
    console.log('🌱 Cosecha en Cope - Landing page initialized');
});

/**
 * Smooth scrolling for anchor links
 */
function initSmoothScrolling() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

/**
 * Intersection Observer for scroll animations
 */
function initScrollAnimations() {
    // Check if Intersection Observer is supported
    if (!('IntersectionObserver' in window)) {
        // Fallback: show all elements immediately
        document.querySelectorAll('.feature-card, .category-card, .story-card, .commitment-card').forEach(el => {
            el.classList.add('animate-in');
        });
        return;
    }
    
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
                // Unobserve after animation to improve performance
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    // Observe elements that should animate in
    document.querySelectorAll('.feature-card, .category-card, .story-card, .commitment-card').forEach(card => {
        observer.observe(card);
    });
}

/**
 * Navbar scroll behavior
 */
function initNavbarBehavior() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;
    
    let lastScrollY = window.scrollY;
    let ticking = false;
    
    function updateNavbar() {
        const currentScrollY = window.scrollY;
        
        if (currentScrollY > 100) {
            navbar.classList.add('navbar-scrolled');
        } else {
            navbar.classList.remove('navbar-scrolled');
        }
        
        lastScrollY = currentScrollY;
        ticking = false;
    }
    
    function onScroll() {
        if (!ticking) {
            requestAnimationFrame(updateNavbar);
            ticking = true;
        }
    }
    
    window.addEventListener('scroll', onScroll, { passive: true });
}

/**
 * Lazy loading for images
 */
function initLazyLoading() {
    // Check if native lazy loading is supported
    if ('loading' in HTMLImageElement.prototype) {
        // Native lazy loading is supported
        document.querySelectorAll('img[data-src]').forEach(img => {
            img.src = img.dataset.src;
            img.removeAttribute('data-src');
        });
    } else {
        // Fallback to Intersection Observer
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        img.src = img.dataset.src;
                        img.classList.remove('lazy');
                        imageObserver.unobserve(img);
                    }
                });
            });
            
            document.querySelectorAll('img[data-src]').forEach(img => {
                imageObserver.observe(img);
            });
        } else {
            // Fallback: load all images immediately
            document.querySelectorAll('img[data-src]').forEach(img => {
                img.src = img.dataset.src;
                img.removeAttribute('data-src');
            });
        }
    }
}

/**
 * Performance optimizations
 */
function initPerformanceOptimizations() {
    // Preload critical resources
    preloadCriticalResources();
    
    // Optimize third-party scripts
    optimizeThirdPartyScripts();
    
    // Monitor performance
    monitorPerformance();
}

/**
 * Preload critical resources
 */
function preloadCriticalResources() {
    // Preload critical images on hover
    document.querySelectorAll('a[href^="/app/"]').forEach(link => {
        link.addEventListener('mouseenter', () => {
            // Preload Angular app resources
            if (!link.dataset.preloaded) {
                const preloadLink = document.createElement('link');
                preloadLink.rel = 'prefetch';
                preloadLink.href = '/app/';
                document.head.appendChild(preloadLink);
                link.dataset.preloaded = 'true';
            }
        });
    });
}

/**
 * Optimize third-party scripts
 */
function optimizeThirdPartyScripts() {
    // Delay non-critical third-party scripts
    window.addEventListener('load', () => {
        // Load analytics or other third-party scripts here
        // after the main page has loaded
    });
}

/**
 * Monitor performance
 */
function monitorPerformance() {
    // Core Web Vitals monitoring
    if ('PerformanceObserver' in window) {
        // Monitor Largest Contentful Paint (LCP)
        new PerformanceObserver((entryList) => {
            for (const entry of entryList.getEntries()) {
                console.log('LCP:', entry.startTime);
            }
        }).observe({ entryTypes: ['largest-contentful-paint'] });
        
        // Monitor First Input Delay (FID)
        new PerformanceObserver((entryList) => {
            for (const entry of entryList.getEntries()) {
                console.log('FID:', entry.processingStart - entry.startTime);
            }
        }).observe({ entryTypes: ['first-input'] });
        
        // Monitor Cumulative Layout Shift (CLS)
        new PerformanceObserver((entryList) => {
            let clsValue = 0;
            for (const entry of entryList.getEntries()) {
                if (!entry.hadRecentInput) {
                    clsValue += entry.value;
                }
            }
            console.log('CLS:', clsValue);
        }).observe({ entryTypes: ['layout-shift'] });
    }
}

/**
 * Newsletter subscription (if needed)
 */
function initNewsletterSubscription() {
    const newsletterForm = document.querySelector('.newsletter-form');
    if (!newsletterForm) return;
    
    newsletterForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const email = this.querySelector('input[type="email"]').value;
        const button = this.querySelector('button[type="submit"]');
        
        // Disable button and show loading state
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
        
        // Simulate API call
        setTimeout(() => {
            // Reset button
            button.disabled = false;
            button.innerHTML = '<i class="fas fa-paper-plane"></i>';
            
            // Show success message
            const successAlert = document.createElement('div');
            successAlert.className = 'alert alert-success alert-dismissible fade show mt-2';
            successAlert.innerHTML = `
                <i class="fas fa-check-circle me-2"></i>
                ¡Gracias! Te has suscrito correctamente.
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            `;
            
            this.appendChild(successAlert);
            this.reset();
        }, 1500);
    });
}

/**
 * Utility functions
 */
const utils = {
    // Debounce function for performance
    debounce: function(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },
    
    // Throttle function for scroll events
    throttle: function(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    },
    
    // Check if element is in viewport
    isInViewport: function(element) {
        const rect = element.getBoundingClientRect();
        return (
            rect.top >= 0 &&
            rect.left >= 0 &&
            rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
            rect.right <= (window.innerWidth || document.documentElement.clientWidth)
        );
    }
};

// Export utils for global use
window.CosechaEnCopeUtils = utils;