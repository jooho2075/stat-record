package io.github.jooho2075.statrecord.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(@NotBlank @Size(max = 100) String name) {
}
