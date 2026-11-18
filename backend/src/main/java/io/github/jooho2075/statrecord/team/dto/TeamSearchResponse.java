package io.github.jooho2075.statrecord.team.dto;

import java.util.List;

public record TeamSearchResponse(Long teamId, String teamName, List<String> managerNames, long memberCount) {
}
