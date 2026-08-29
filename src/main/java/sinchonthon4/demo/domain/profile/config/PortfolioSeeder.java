package sinchonthon4.demo.domain.profile.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.profile.entity.Portfolio;
import sinchonthon4.demo.domain.profile.repository.PortfolioRepository;
import sinchonthon4.demo.domain.user.entity.User;
import sinchonthon4.demo.domain.user.repository.UserRepository;

/**
 * 개발/시연용 시드.
 * img 폴더의 이미지들을 user_id=1 의 포트폴리오 한 건에 순서대로 넣는다.
 * StaticResourceConfig 가 /img/** 로 서빙하므로 브라우저에서 바로 볼 수 있다.
 * 이미 해당 유저의 포트폴리오가 있으면 건너뛴다(중복 시드 방지).
 */
@Component
@Order(5)
@RequiredArgsConstructor
public class PortfolioSeeder implements ApplicationRunner {

    private static final Long SEED_USER_ID = 1L;
    private static final String SEED_TITLE = "포트폴리오";

    // img 폴더의 파일명(정렬 순서 = 노출 순서)
    private static final List<String> IMAGE_FILE_NAMES = List.of(
            "14.png",
            "15.png",
            "18.png",
            "19.png",
            "20.png",
            "21.png",
            "23.png",
            "24.png",
            "25.png",
            "26.png",
            "27.png"
    );

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository.findById(SEED_USER_ID).orElse(null);
        if (user == null) {
            return;
        }
        if (!portfolioRepository.findAllByUser_IdOrderByCreatedAtDescIdDesc(SEED_USER_ID).isEmpty()) {
            return;
        }

        List<String> imageUrls = IMAGE_FILE_NAMES.stream()
                .map(PortfolioSeeder::toImageUrl)
                .toList();

        portfolioRepository.save(Portfolio.create(user, SEED_TITLE, null, imageUrls));
    }

    private static String toImageUrl(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "/img/" + encoded;
    }
}
