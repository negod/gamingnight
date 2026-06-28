package se.backede.infrastructure.security;

import se.backede.application.dto.AuthenticatedUser;
import se.backede.application.usecase.TokenService;
import se.backede.domain.model.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            authenticate(request);
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }

    private void authenticate(HttpServletRequest request) {
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()
                && !(existing instanceof AnonymousAuthenticationToken)) {
            populateAuthContextFrom(existing);
            return;
        }

        String token = bearerToken(request);
        if (token == null) {
            return;
        }

        tokenService.parse(token).ifPresent(user -> {
            AuthContext.set(user);
            var authority = new SimpleGrantedAuthority("ROLE_" + user.role().name());
            var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(auth);
        });
    }

    private static void populateAuthContextFrom(Authentication auth) {
        if (auth.getPrincipal() instanceof AuthenticatedUser user) {
            AuthContext.set(user);
            return;
        }
        // @WithMockUser principal is a String username — synthesise an AuthenticatedUser for controllers that read AuthContext
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        AuthContext.set(new AuthenticatedUser(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                auth.getName(),
                isAdmin ? UserRole.ADMIN : UserRole.USER,
                UUID.fromString("00000000-0000-0000-0000-000000000002")
        ));
    }

    private static String bearerToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length());
    }
}
