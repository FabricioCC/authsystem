//package com.fabriciodev.authsystem.filter;
//
//import com.fabriciodev.authsystem.service.TokenService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//    private final TokenService tokenService;
//
//    public JwtAuthFilter(TokenService tokenService) {
//        this.tokenService = tokenService;
//    }
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain) throws ServletException, IOException {
//
//        String token = extractToken(request);
//
//        // Sem token? Segue o filtro sem autenticar.
//        // O Spring Security vai barrar endpoints protegidos com 401.
//        if (token == null) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // Token inválido, expirado ou na blacklist?
//        // Mesma coisa — segue sem autenticar, não retorna erro aqui.
//        // Quem retorna o 401 é o AuthenticationEntryPoint configurado no SecurityConfig.
//        if (!tokenService.validateToken(token)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // Só seta o contexto se ainda não houver autenticação.
//        // Evita sobrescrever em cenários de múltiplos filtros.
//        if (SecurityContextHolder.getContext().getAuthentication() == null) {
//            UsernamePasswordAuthenticationToken authentication =
//                    buildAuthentication(token, request);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    // ------------------------------------------------------------------ privado
//
//    /**
//     * Extrai o token do header Authorization: Bearer <token>.
//     * Retorna null se o header não existir ou não começar com "Bearer ".
//     */
//    private String extractToken(HttpServletRequest request) {
//        String header = request.getHeader("Authorization");
//        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
//            return header.substring(7); // remove "Bearer "
//        }
//        return null;
//    }
//
//    /**
//     * Monta o objeto de autenticação com roles e permissions como authorities.
//     *
//     * Convenção do Spring Security:
//     *   - roles viram "ROLE_ADMIN", "ROLE_USER" (prefixo ROLE_ para hasRole())
//     *   - permissions ficam como estão: "READ_USERS", "DELETE_POST" (para hasAuthority())
//     */
//    private UsernamePasswordAuthenticationToken buildAuthentication(
//            String token, HttpServletRequest request) {
//
//        String userId = tokenService.extractSubject(token);
//        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//
//        // Roles com prefixo ROLE_ — compatível com hasRole("ADMIN") no SecurityConfig
//        List<String> roles = tokenService.extractRoles(token);
//        if (roles != null) {
//            roles.stream()
//                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                    .forEach(authorities::add);
//        }
//
//        // Permissions sem prefixo — compatível com hasAuthority("DELETE_POST")
//        List<String> permissions = tokenService.extractPermissions(token);
//        if (permissions != null) {
//            permissions.stream()
//                    .map(SimpleGrantedAuthority::new)
//                    .forEach(authorities::add);
//        }
//
//        UsernamePasswordAuthenticationToken authentication =
//                new UsernamePasswordAuthenticationToken(userId, null, authorities);
//
//        // Adiciona detalhes do request (IP, session ID) — útil para auditoria
//        authentication.setDetails(
//                new WebAuthenticationDetailsSource().buildDetails(request)
//        );
//
//        return authentication;
//    }
//
//    /**
//     * Rotas públicas não precisam passar pela validação.
//     * O Spring Security já cuida disso via permitAll() no SecurityConfig,
//     * mas pulá-las aqui evita parsing de JWT desnecessário.
//     */
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//        return path.startsWith("/auth/login")
//                || path.startsWith("/auth/register")
//                || path.startsWith("/auth/forgot-password")
//                || path.startsWith("/auth/reset-password")
//                || path.startsWith("/swagger-ui")
//                || path.startsWith("/v3/api-docs");
//    }
//}
