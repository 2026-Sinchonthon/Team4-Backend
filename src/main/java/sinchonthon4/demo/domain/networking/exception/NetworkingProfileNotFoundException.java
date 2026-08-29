package sinchonthon4.demo.domain.networking.exception;

public class NetworkingProfileNotFoundException extends RuntimeException {

    public NetworkingProfileNotFoundException(Long userId) {
        super("사용자 프로필을 찾을 수 없습니다. userId=" + userId);
    }
}
