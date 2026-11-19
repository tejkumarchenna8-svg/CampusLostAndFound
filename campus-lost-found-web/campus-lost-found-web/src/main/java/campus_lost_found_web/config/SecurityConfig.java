package campus_lost_found_web.config;

import campus_lost_found_web.repository.StudentRepository;
import campus_lost_found_web.service.StudentUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain studentSecurityFilterChain(HttpSecurity http,
                                                   StudentRepository studentRepository) throws Exception {
        StudentUserDetailsService studentDetailsService = new StudentUserDetailsService(studentRepository);
        DaoAuthenticationProvider studentProvider = new DaoAuthenticationProvider(studentDetailsService);
        studentProvider.setPasswordEncoder(new BCryptPasswordEncoder());

        http.securityMatcher("/student/**")
                .authenticationProvider(studentProvider)
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/student/login").permitAll()
                        .anyRequest().hasRole("USER"))
                .formLogin(form -> form
                        .loginPage("/student/login")
                        .loginProcessingUrl("/student/login")
                        .defaultSuccessUrl("/student/dashboard", true)
                        .failureUrl("/student/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/student/logout")
                        .logoutSuccessUrl("/student/login?logout"));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Keep Spring Boot's global admin UserDetailsService and generated local password intact.
        http.csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/admin")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/search", true)
                        .failureUrl("/admin?error")
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/admin?logout"))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}