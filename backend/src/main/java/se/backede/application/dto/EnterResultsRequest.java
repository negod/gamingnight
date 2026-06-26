package se.backede.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EnterResultsRequest(
        @NotNull(message = "Results list is required")
        List<@Valid PlayerResultInput> results
) {
}
