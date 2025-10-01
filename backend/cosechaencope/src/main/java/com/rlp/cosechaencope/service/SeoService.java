package com.rlp.cosechaencope.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Servicio para optimización SEO y generación de datos estructurados
 * 
 * @author rafalopezzz
 * @since 1.0.0
 */
@Service
public class SeoService {

    /**
     * Genera datos estructurados JSON-LD para la página principal
     */
    public String getHomePageStructuredData() {
        return """
            {
                "@context": "https://schema.org",
                "@type": "Organization",
                "name": "Cosecha en Cope",
                "description": "Plataforma que conecta productores agrícolas locales con consumidores conscientes",
                "url": "https://cosechaencope.com",
                "logo": "https://cosechaencope.com/images/logo.png",
                "contactPoint": {
                    "@type": "ContactPoint",
                    "telephone": "+34-XXX-XXX-XXX",
                    "contactType": "Customer Service",
                    "availableLanguage": "Spanish"
                },
                "sameAs": [
                    "https://facebook.com/cosechaencope",
                    "https://instagram.com/cosechaencope"
                ],
                "address": {
                    "@type": "PostalAddress",
                    "addressCountry": "ES",
                    "addressRegion": "Andalucía"
                }
            }
            """;
    }

    /**
     * Obtiene un preview de categorías para la página de categorías SEO
     */
    public List<Map<String, Object>> getCategoriesPreview() {
        return List.of(
            Map.of(
                "name", "Frutas",
                "description", "Frutas frescas de temporada",
                "image", "/images/categories/frutas.jpg",
                "slug", "frutas"
            ),
            Map.of(
                "name", "Verduras",
                "description", "Verduras locales y orgánicas",
                "image", "/images/categories/verduras.jpg",
                "slug", "verduras"
            ),
            Map.of(
                "name", "Hortalizas",
                "description", "Hortalizas frescas del campo",
                "image", "/images/categories/hortalizas.jpg",
                "slug", "hortalizas"
            ),
            Map.of(
                "name", "Cereales",
                "description", "Cereales y granos naturales",
                "image", "/images/categories/cereales.jpg",
                "slug", "cereales"
            )
        );
    }

    /**
     * Obtiene productos de muestra para preview SEO por categoría
     */
    public List<Map<String, Object>> getProductsPreview(String categoria) {
        // En una implementación real, esto haría consulta a la base de datos
        // Por ahora devolvemos datos de muestra para SEO
        return List.of(
            Map.of(
                "name", String.format("Producto Premium de %s", categoria),
                "description", String.format("El mejor producto de %s de la región", categoria),
                "price", "5.99",
                "image", String.format("/images/products/%s-sample-1.jpg", categoria),
                "producer", "Productor Local"
            ),
            Map.of(
                "name", String.format("Selección Especial de %s", categoria),
                "description", String.format("Selección especial de %s cultivados localmente", categoria),
                "price", "8.50",
                "image", String.format("/images/products/%s-sample-2.jpg", categoria),
                "producer", "Granja Familiar"
            ),
            Map.of(
                "name", String.format("%s Orgánicos", categoria),
                "description", String.format("%s 100%% orgánicos certificados", categoria),
                "price", "12.00",
                "image", String.format("/images/products/%s-sample-3.jpg", categoria),
                "producer", "Cultivos Sostenibles"
            )
        );
    }

    /**
     * Genera datos estructurados para productos
     */
    public String getProductStructuredData(String productName, String description, 
                                         String price, String category) {
        return String.format("""
            {
                "@context": "https://schema.org",
                "@type": "Product",
                "name": "%s",
                "description": "%s",
                "category": "%s",
                "offers": {
                    "@type": "Offer",
                    "price": "%s",
                    "priceCurrency": "EUR",
                    "availability": "https://schema.org/InStock"
                },
                "brand": {
                    "@type": "Brand",
                    "name": "Cosecha en Cope"
                }
            }
            """, productName, description, category, price);
    }

    /**
     * Genera metadatos Open Graph dinámicos
     */
    public Map<String, String> getOpenGraphData(String title, String description, 
                                              String url, String image) {
        return Map.of(
            "og:title", title,
            "og:description", description,
            "og:url", url,
            "og:image", image,
            "og:type", "website",
            "og:site_name", "Cosecha en Cope"
        );
    }
}