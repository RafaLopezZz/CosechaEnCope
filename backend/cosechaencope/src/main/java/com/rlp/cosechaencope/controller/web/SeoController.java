package com.rlp.cosechaencope.controller.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Controlador para recursos SEO como sitemap.xml y robots.txt
 * 
 * @author rafalopezzz
 * @since 1.0.0
 */
@Controller
@Hidden // Ocultar de Swagger ya que son recursos SEO, no API
public class SeoController {

    /**
     * Genera sitemap.xml dinámicamente para mejor indexación
     */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                <url>
                    <loc>https://cosechaencope.com/</loc>
                    <lastmod>2025-10-10</lastmod>
                    <changefreq>daily</changefreq>
                    <priority>1.0</priority>
                </url>
                <url>
                    <loc>https://cosechaencope.com/nosotros</loc>
                    <lastmod>2025-10-10</lastmod>
                    <changefreq>monthly</changefreq>
                    <priority>0.8</priority>
                </url>
                <url>
                    <loc>https://cosechaencope.com/contacto</loc>
                    <lastmod>2025-10-10</lastmod>
                    <changefreq>monthly</changefreq>
                    <priority>0.7</priority>
                </url>
                <url>
                    <loc>https://cosechaencope.com/categorias</loc>
                    <lastmod>2025-10-10</lastmod>
                    <changefreq>weekly</changefreq>
                    <priority>0.9</priority>
                </url>
                <url>
                    <loc>https://cosechaencope.com/productos/frutas</loc>
                    <lastmod>2025-10-10</lastmod>
                    <changefreq>weekly</changefreq>
                    <priority>0.8</priority>
                </url>
                <url>
                    <loc>https://cosechaencope.com/productos/verduras</loc>
                    <lastmod>2025-10-10</lastmod>
                    <changefreq>weekly</changefreq>
                    <priority>0.8</priority>
                </url>
                <url>
                    <loc>https://cosechaencope.com/productos/hortalizas</loc>
                    <lastmod>2025-10-10</lastmod>
                    <changefreq>weekly</changefreq>
                    <priority>0.8</priority>
                </url>
                <url>
                    <loc>https://cosechaencope.com/productos/cereales</loc>
                    <lastmod>2025-10-10</lastmod>
                    <changefreq>weekly</changefreq>
                    <priority>0.8</priority>
                </url>
            </urlset>
            """;
    }

    /**
     * Genera robots.txt para guiar a los web crawlers
     */
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        return """
            User-agent: *
            Allow: /
            
            # Sitemap location
            Sitemap: https://cosechaencope.com/sitemap.xml
            
            # Optimized crawling
            Crawl-delay: 1
            
            # Allow access to Angular SPA for JavaScript crawlers
            Allow: /app/
            
            # Allow static resources
            Allow: /css/
            Allow: /js/
            Allow: /images/
            
            # Disallow admin areas (if any in future)
            Disallow: /admin/
            Disallow: /api/admin/
            """;
    }

    /**
     * Endpoint para verificación de Google Search Console
     * (Reemplazar con el código real de verificación cuando sea necesario)
     */
    @GetMapping(value = "/google-site-verification.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String googleSiteVerification() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Google Site Verification</title>
                <meta name="google-site-verification" content="REPLACE_WITH_ACTUAL_VERIFICATION_CODE" />
            </head>
            <body>
                <h1>Google Site Verification</h1>
                <p>Esta página es para verificación de Google Search Console.</p>
            </body>
            </html>
            """;
    }
}