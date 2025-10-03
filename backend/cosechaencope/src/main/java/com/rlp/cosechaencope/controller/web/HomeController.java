package com.rlp.cosechaencope.controller.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rlp.cosechaencope.dto.response.ArticuloResponse;
import com.rlp.cosechaencope.dto.response.CategoriaResponse;
import com.rlp.cosechaencope.service.ArticuloService;
import com.rlp.cosechaencope.service.CategoriaService;
import com.rlp.cosechaencope.service.SeoService;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para páginas web renderizadas server-side con Thymeleaf
 * Optimizado para SEO y rendimiento de carga inicial
 * 
 * @author rafalopezzz
 * @since 1.0.0
 */
@Slf4j
@Controller
@RequestMapping
@Hidden // Ocultar de Swagger ya que son páginas web, no API
public class HomeController {

    @Autowired
    private SeoService seoService;
    
    @Autowired
    private CategoriaService categoriaService;
    
    @Autowired
    private ArticuloService articuloService;

    /**
     * Landing page principal - Renderizada server-side para SEO
     * Incluye categorías y artículos destacados para mejor experiencia
     */
    @GetMapping("/")
    public String landing(Model model, HttpServletRequest request) {
        log.info("Renderizando landing page con datos dinámicos");
        
        try {
            // 1. Obtener categorías (máximo 6 para la landing)
            List<CategoriaResponse> categorias = categoriaService.obtenerTodasLasCategorias()
                .stream()
                .limit(6)
                .collect(Collectors.toList());
            model.addAttribute("categorias", categorias);
            log.debug("Categorías cargadas: {}", categorias.size());
            
            // 2. Obtener artículos (8 primeros con imagen válida)
            List<ArticuloResponse> articulos = articuloService.obtenerTodosLosArticulos()
                .stream()
                .filter(a -> a.getImagenUrl() != null && !a.getImagenUrl().trim().isEmpty())
                .limit(8)
                .collect(Collectors.toList());
            model.addAttribute("articulos", articulos);
            log.debug("Artículos cargados: {}", articulos.size());
            
        } catch (Exception e) {
            log.error("Error al cargar datos dinámicos para landing: {}", e.getMessage(), e);
            // Si falla, mostrar landing sin datos dinámicos (empty state se mostrará)
            model.addAttribute("categorias", List.of());
            model.addAttribute("articulos", List.of());
        }
        
        // Configurar meta datos para SEO
        model.addAttribute("pageTitle", "Cosecha en Cope - Productos Agrícolas Frescos Directos del Campo");
        model.addAttribute("pageDescription", 
            "Conectamos productores locales con consumidores conscientes. " +
            "Descubre productos agrícolas frescos, de temporada y sostenibles " +
            "directamente de los campos de nuestra región.");
        model.addAttribute("canonicalUrl", "https://cosechaencope.com/");
        model.addAttribute("ogImage", "/images/og-landing.jpg");
        
        // Datos estructurados para rich snippets
        model.addAttribute("structuredData", seoService.getHomePageStructuredData());
        
        // Keywords para SEO
        model.addAttribute("keywords", 
            "productos agrícolas, frutas frescas, verduras locales, " +
            "productores locales, agricultura sostenible, alimentos frescos");
        
        // URI actual para navegación activa
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/landing";
    }

    /**
     * Página "Nosotros" - Información de la empresa
     */
    @GetMapping("/nosotros")
    public String about(Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Sobre Nosotros - Cosecha en Cope");
        model.addAttribute("pageDescription", 
            "Conoce nuestra misión de conectar productores locales con consumidores " +
            "que valoran la calidad, frescura y sostenibilidad de los productos agrícolas.");
        model.addAttribute("canonicalUrl", "https://cosechaencope.com/nosotros");
        model.addAttribute("ogImage", "/images/og-about.jpg");
        
        // URI actual para navegación activa
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/about";
    }

    /**
     * Página de contacto
     */
    @GetMapping("/contacto")
    public String contact(Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Contacto - Cosecha en Cope");
        model.addAttribute("pageDescription", 
            "¿Tienes preguntas? Contáctanos. Estamos aquí para ayudarte con " +
            "cualquier consulta sobre nuestros productos o servicios.");
        model.addAttribute("canonicalUrl", "https://cosechaencope.com/contacto");
        model.addAttribute("ogImage", "/images/og-contact.jpg");
        
        // URI actual para navegación activa
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/contact";
    }

    /**
     * Preview de categorías para SEO (sin funcionalidad completa)
     * La funcionalidad completa está en Angular SPA
     */
    @GetMapping("/categorias")
    public String categoriesPreview(Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Categorías de Productos - Cosecha en Cope");
        model.addAttribute("pageDescription", 
            "Explora nuestras categorías de productos agrícolas frescos: " +
            "frutas de temporada, verduras locales, hortalizas orgánicas y más.");
        model.addAttribute("canonicalUrl", "https://cosechaencope.com/categorias");
        model.addAttribute("ogImage", "/images/og-categories.jpg");
        
        // Obtener categorías para mostrar en preview
        model.addAttribute("categories", seoService.getCategoriesPreview());
        
        // URI actual para navegación activa
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/categories-preview";
    }

    /**
     * Preview de productos por categoría para SEO
     */
    @GetMapping("/productos/{categoria}")
    public String productsPreview(@PathVariable String categoria, Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", 
            String.format("Productos de %s - Cosecha en Cope", 
                categoria.substring(0, 1).toUpperCase() + categoria.substring(1)));
        model.addAttribute("pageDescription", 
            String.format("Descubre los mejores productos de %s disponibles en nuestra plataforma. " +
                "Frescos, locales y de la mejor calidad.", categoria));
        model.addAttribute("canonicalUrl", 
            String.format("https://cosechaencope.com/productos/%s", categoria));
        model.addAttribute("ogImage", "/images/og-products.jpg");
        
        // Obtener algunos productos de muestra para SEO
        model.addAttribute("categoryName", categoria);
        model.addAttribute("sampleProducts", seoService.getProductsPreview(categoria));
        
        // URI actual para navegación activa
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "pages/products-preview";
    }

    /**
     * ELIMINADO: Mapping /app/** conflictivo que causaba StackOverflowError
     * Ahora AngularSpaController se encarga exclusivamente de /app/**
     * y sirve index.html directamente sin forwards internos.
     * 
     * HISTORIAL:
     * - Anterior: @GetMapping("/app/**") con forward:/app/index.html
     * - Problema: Bucle infinito → StackOverflowError
     * - Solución: Eliminado, AngularSpaController maneja todas las rutas /app/**
     */

    /**
     * Redirecciones de rutas antiguas de Angular a la nueva estructura
     */
    @GetMapping("/articulos")
    public String redirectArticulos() {
        return "redirect:/app/articulos";
    }

    @GetMapping("/auth")
    public String redirectAuth() {
        return "redirect:/app/auth";
    }

    @GetMapping("/cliente/**")
    public String redirectCliente() {
        return "redirect:/app/cliente/";
    }

    @GetMapping("/productor/**")
    public String redirectProductor() {
        return "redirect:/app/productor/";
    }
}