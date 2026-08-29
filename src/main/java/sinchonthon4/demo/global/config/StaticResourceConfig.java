package sinchonthon4.demo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 로컬 img 디렉터리의 이미지를 /img/** 경로로 브라우저에서 접근 가능하게 서빙한다.
 * 예) 프로젝트 루트 img/14.png -> http://localhost:8080/img/14.png
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    // 애플리케이션 실행 디렉터리 기준 상대 경로. 필요 시 app.image.location 으로 override.
    @Value("${app.image.location:img}")
    private String imageLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = imageLocation.endsWith("/") ? imageLocation : imageLocation + "/";
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:" + location);
    }
}
