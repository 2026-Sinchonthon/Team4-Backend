package sinchonthon4.demo.domain.home.exception;

public class HomeProfileNotFoundException extends RuntimeException {

    public HomeProfileNotFoundException(Long userId) {
        super("홈을 조회할 사용자 프로필을 찾을 수 없습니다. userId=" + userId);
    }
}
