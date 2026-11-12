package pe.edu.upeu.mana.config;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import pe.edu.upeu.mana.security.*;
import pe.edu.upeu.mana.repository.UsuarioRepository;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwt, UsuarioDetailsService detalles, UsuarioRepository usuarios, RestAuthenticationEntryPoint entry, RestAccessDeniedHandler denied, @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(c -> c.configurationSource(cors))
                .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/recuperar", "/api/v1/auth/restablecer").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/auth/me", "/api/v1/catalogos").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/comprobantes/*/reenviar").hasAuthority("COMPROBANTE_GESTIONAR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/comprobantes/**").hasAuthority("COMPROBANTE_EMITIR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/comprobantes/**").hasAuthority("COMPROBANTE_VER")
                        .requestMatchers("/api/v1/usuarios/**").hasAuthority("USUARIO_GESTIONAR")
                        .requestMatchers("/api/v1/locales/**").hasAuthority("LOCAL_GESTIONAR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/*/contrato-vigente").hasAuthority("CONTRATO_LEER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**").hasAuthority("CLIENTE_LEER")
                        .requestMatchers("/api/v1/clientes/**").hasAuthority("CLIENTE_ESCRIBIR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/contratos/**", "/api/v1/pensionistas/**").hasAuthority("CONTRATO_LEER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/contratos/*/anular").hasAuthority("CONTRATO_ANULAR")
                        .requestMatchers("/api/v1/contratos/**").hasAuthority("CONTRATO_REGISTRAR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/consumos/*/anular").hasAuthority("CONSUMO_ANULAR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/consumos/**").hasAuthority("CONTRATO_LEER")
                        .requestMatchers("/api/v1/consumos/**").hasAuthority("CONSUMO_REGISTRAR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/ventas/*/anular").hasAuthority("VENTA_ANULAR")
                        .requestMatchers("/api/v1/ventas/**", "/api/v1/pos/**").hasAuthority("VENTA_REGISTRAR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cajas").hasAuthority("CAJA_SUPERVISAR")
                        .requestMatchers("/api/v1/cajas/**").hasAuthority("CAJA_OPERAR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/platos/**", "/api/v1/menus/**", "/api/v1/reglas-cobro/**", "/api/v1/tarifas/**", "/api/v1/precios-sueltos/**").hasAuthority("MENU_LEER")
                        .requestMatchers("/api/v1/platos/**", "/api/v1/menus/**", "/api/v1/reglas-cobro/**", "/api/v1/tarifas/**", "/api/v1/precios-sueltos/**").hasAuthority("MENU_GESTIONAR")
                        .requestMatchers("/api/v1/planes/**").hasAuthority("PLAN_GESTIONAR")
                        .requestMatchers("/api/v1/dias-no-atencion/**").hasAuthority("CALENDARIO_GESTIONAR")
                        .requestMatchers("/api/v1/bebidas/**", "/api/v1/proveedores/**", "/api/v1/compras-bebidas/**", "/api/v1/stock-bebidas/**", "/api/v1/movimientos-stock/**").hasAuthority("INVENTARIO_GESTIONAR")
                        .requestMatchers("/api/v1/gastos/**").hasAuthority("GASTO_GESTIONAR")
                        .requestMatchers("/api/v1/reportes/**").hasAuthority("REPORTE_VER")
                        // Autocompletado: DNI en clientes y usuarios, RUC en proveedores.
                        .requestMatchers(HttpMethod.GET, "/api/v1/consultas/dni/*").hasAnyAuthority("CLIENTE_ESCRIBIR", "USUARIO_GESTIONAR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/consultas/ruc/*").hasAuthority("INVENTARIO_GESTIONAR")
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwt, detalles, usuarios, entry), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
