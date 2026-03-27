package com.example.microquest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for MicroQuest.
 * <p>
 * Currently responsible for declaring (or explicitly <em>not</em> declaring)
 * static-resource handler mappings for user-uploaded files:
 * <ul>
 *   <li>GIF submissions are intentionally served through a secured controller
 *       endpoint ({@code /quests/{id}/submissions/{subId}/gif}) rather than via
 *       a public static-file handler, so that access-control checks can be
 *       applied per request.</li>
 *   <li>Photo IDs are served through the admin-only
 *       {@code /admin/photo-id/{filename}} endpoint.</li>
 * </ul>
 * Additional MVC customisations (interceptors, argument resolvers, etc.) should
 * be added here as the application grows.
 * </p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Filesystem path to the root uploads directory, injected from
     * {@code storage.location} in {@code application.properties}.
     */
    private final String storageLocation;

    /** Injects the configured storage location at construction time. */
    public WebMvcConfig(@Value("${storage.location:uploads}") String storageLocation) {
        this.storageLocation = storageLocation;
    }

    /**
     * Deliberately empty: uploaded files (GIFs and photo IDs) are served through
     * secured controller endpoints rather than an open static-resource mapping,
     * ensuring Spring Security access checks are applied to every file request.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // GIF uploads are served via a secured controller endpoint — no public static handler
    }
}
