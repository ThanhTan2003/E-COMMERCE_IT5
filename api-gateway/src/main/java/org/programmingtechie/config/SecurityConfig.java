package org.programmingtechie.config;

import org.programmingtechie.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
        @Autowired
        private JwtAuthFilter authFilter;

        @Bean
        // authentication
        public UserDetailsService userDetailsService() {
                UserDetails admin = User.withUsername("admin")
                                .password("test123")
                                .roles("ADMIN")
                                .build();
                UserDetails user = User.withUsername("user")
                                .password("test123")
                                .roles("USER")
                                .build();
                return new InMemoryUserDetailsManager(admin, user);
                // return new UserInfoUserDetailsService();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(requests -> requests
                                                .requestMatchers("/products/new", "/products/authenticate").permitAll())
                                .authorizeHttpRequests(requests -> requests.requestMatchers("/products/**")
                                                .authenticated())
                                .sessionManagement(management -> management
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
                authenticationProvider.setUserDetailsService(userDetailsService());
                authenticationProvider.setPasswordEncoder(passwordEncoder());
                return authenticationProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        // @Bean
        // public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity
        // serverHttpSecurity) {
        // serverHttpSecurity
        // .csrf(ServerHttpSecurity.CsrfSpec::disable)
        // .authorizeExchange(exchange ->
        // exchange.pathMatchers("/eureka/**")
        // .permitAll()
        // .anyExchange()
        // .authenticated())
        // .oauth2ResourceServer(spec -> spec.jwt(Customizer.withDefaults()));
        // return serverHttpSecurity.build();
        // }
}
