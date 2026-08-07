package com.example.eventapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public SecurityConfig(CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/business/create",
                                "/business/edit/**",
                                "/business/delete/**",
                                "/dashboard",
                                "/business/{id}/availability/block",
                                "/business/{id}/gallery/upload",
                                "/business/gallery/delete/**",
                                "/business/{id}/availability/toggle"
                        ).hasRole("BUSINESS")

                        .requestMatchers(
                                "/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/favorites/**",
                                "/reviews/**",
                                "/profile",
                                "/business/{id}/reviews",
                                "/notifications/**"
                        ).authenticated()

                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/businesses",
                                "/businesses/category/**",
                                "/css/**",
                                "/js/**",
                                "/uploads/businesses/**",
                                "/privacy",
                                "/terms",
                                "/forgot"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/business/{uuid}").permitAll()

                        .anyRequest().denyAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler(customAuthenticationFailureHandler)
                        .defaultSuccessUrl("/businesses", true)
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Configuration
    public static class WebConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {

            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:uploads/");
        }
    }
}
