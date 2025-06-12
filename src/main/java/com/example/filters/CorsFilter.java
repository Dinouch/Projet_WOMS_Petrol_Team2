package com.example.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Filtre CORS (Cross-Origin Resource Sharing) pour autoriser les requêtes cross-origin.
 *
 * <p>Ce filtre permet :</p>
 * <ul>
 *   <li>Les requêtes avec credentials (cookies, auth)</li>
 *   <li>Les méthodes HTTP standards (GET, POST, PUT, DELETE, OPTIONS)</li>
 *   <li>Les en-têtes personnalisés (Content-Type, Authorization, etc.)</li>
 * </ul>
 *
 * <p>Gère automatiquement les requêtes OPTIONS (preflight) en renvoyant une réponse immédiate.</p>
 *
 * <p>Appliqué à toutes les URLs via @WebFilter("/*") pour une configuration centralisée.</p>
 */
@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Pas besoin d'initialisation
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Autoriser les requêtes venant de React (localhost:3000)
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, Authorization");

        // Si c'est une requête preflight (OPTIONS), on renvoie directement OK
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Sinon, on continue la chaîne
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Pas besoin de destruction
    }
}
