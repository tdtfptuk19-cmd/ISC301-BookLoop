package com.bookloop.config;

import com.bookloop.service.BookloopUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration for BookLoop.
 * Uses modern lambda-style configuration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BookloopUserDetailsService userDetailsService;

    public SecurityConfig(BookloopUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * BCrypt password encoder bean.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Defines the security filter chain — access rules, login, and logout.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/webhooks/**")
            )
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public pages: static assets, home, listings, public seller profile, login, registration, payment webhooks
                .requestMatchers(
                        "/",
                        "/books",
                        "/books/**",
                        "/textbooks",
                        "/textbooks/**",
                        "/profile/seller/**",
                        "/register",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/error",
                        "/api/webhooks/**"
                ).permitAll()
                // Admin pages: require ADMIN role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Protected pages: cart, checkout, orders, reviews, profile, listings CRUD, seller finance, buyer payment
                .anyRequest().authenticated()
            )
            // Form login configuration
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", false)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // Logout configuration
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // CSRF is enabled by default
            .userDetailsService(userDetailsService);

        return http.build();
    }
}
