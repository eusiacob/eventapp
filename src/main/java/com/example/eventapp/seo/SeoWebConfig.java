package com.example.eventapp.seo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SeoWebConfig implements WebMvcConfigurer {
    private final SeoService seo;

    public SeoWebConfig(SeoService seo) { this.seo = seo; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response,
                                   Object handler, ModelAndView view) {
                if (view == null || view.getViewName() == null || view.getViewName().startsWith("redirect:")) return;
                SeoMetadata metadata = response.getStatus() < 400
                        ? seo.forView(view.getViewName(), view.getModel()) : null;
                if (metadata != null) {
                    view.addObject("seo", metadata);
                    response.setHeader("X-Robots-Tag", metadata.robots());
                } else {
                    response.setHeader("X-Robots-Tag", "noindex, nofollow");
                }
            }
        });
    }
}
