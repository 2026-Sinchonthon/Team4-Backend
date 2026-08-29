package sinchonthon4.demo.domain.networking.dto;

import java.util.List;

public record NetworkingProfilePageResponse(
        List<NetworkingProfileSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public NetworkingProfilePageResponse {
        content = List.copyOf(content);
    }
}
