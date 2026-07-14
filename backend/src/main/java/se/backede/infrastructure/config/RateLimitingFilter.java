package se.backede.infrastructure.config;

import se.backede.infrastructure.web.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String CREATE_USER = "create-user";
    private static final String START_COMPETITION = "start-competition";
    private static final String ENTER_RESULTS = "enter-results";

    private static final Pattern START_COMPETITION_PATH =
            Pattern.compile("^/api/competitions/[^/]+/start$");
    private static final Pattern ENTER_RESULTS_PATH =
            Pattern.compile("^/api/competitions/[^/]+/matches/[^/]+/results$");

    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Integer> requestsPerMinuteByEndpoint;

    public RateLimitingFilter(
            ObjectMapper objectMapper,
            @Value("${app.rate-limits.create-user.requests-per-minute:5}") int createUserRequestsPerMinute,
            @Value("${app.rate-limits.start-competition.requests-per-minute:10}") int startCompetitionRequestsPerMinute,
            @Value("${app.rate-limits.enter-results.requests-per-minute:30}") int enterResultsRequestsPerMinute) {
        this.objectMapper = objectMapper;
        this.requestsPerMinuteByEndpoint = Map.of(
                CREATE_USER, createUserRequestsPerMinute,
                START_COMPETITION, startCompetitionRequestsPerMinute,
                ENTER_RESULTS, enterResultsRequestsPerMinute
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String endpoint = rateLimitedEndpoint(request);
        if (endpoint == null) {
            filterChain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = bucketFor(endpoint, request.getRemoteAddr()).tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        writeRateLimitResponse(response, probe);
    }

    private Bucket bucketFor(String endpoint, String remoteAddress) {
        String key = endpoint + ":" + remoteAddress;
        return buckets.computeIfAbsent(key, ignored -> newBucket(requestsPerMinuteByEndpoint.get(endpoint)));
    }

    private static Bucket newBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillIntervally(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private static String rateLimitedEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if ("POST".equals(method) && "/api/users".equals(path)) {
            return CREATE_USER;
        }
        if ("POST".equals(method) && START_COMPETITION_PATH.matcher(path).matches()) {
            return START_COMPETITION;
        }
        if ("PUT".equals(method) && ENTER_RESULTS_PATH.matcher(path).matches()) {
            return ENTER_RESULTS;
        }
        return null;
    }

    private void writeRateLimitResponse(HttpServletResponse response, ConsumptionProbe probe) throws IOException {
        long retryAfterSeconds = Math.max(1, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                new ApiErrorResponse("Too many requests", List.of(), Instant.now()));
    }
}
