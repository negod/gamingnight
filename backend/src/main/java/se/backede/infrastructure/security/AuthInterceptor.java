package se.backede.infrastructure.security;

import se.backede.application.usecase.TokenService;
import se.backede.domain.model.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@ConditionalOnBean(TokenService.class)
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || publicApi(request)) {
            return true;
        }

        var token = bearerToken(request);
        var user = tokenService.parse(token).orElse(null);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return false;
        }

        AuthContext.set(user);
        if (user.role() == UserRole.ADMIN || userAllowed(request)) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private static boolean publicApi(HttpServletRequest request) {
        var uri = request.getRequestURI();
        return uri.equals("/api/auth/login") || uri.equals("/api/auth/signup");
    }

    private static String bearerToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length());
    }

    private static boolean userAllowed(HttpServletRequest request) {
        var path = request.getRequestURI();
        var method = request.getMethod();
        if ("GET".equals(method) && path.equals("/api/users/me")) {
            return true;
        }
        if ("GET".equals(method) && path.startsWith("/api/competitions")) {
            return true;
        }
        if ("GET".equals(method) && path.matches("/api/(games|teams|players)/[^/]+")) {
            return true;
        }
        return false;
    }
}
