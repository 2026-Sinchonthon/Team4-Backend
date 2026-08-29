package sinchonthon4.demo.domain.networking.repository;

import java.util.List;

public record NetworkingProfilePage(List<NetworkingProfileRecord> content, long totalElements) {
    public NetworkingProfilePage {
        content = List.copyOf(content);
    }
}
