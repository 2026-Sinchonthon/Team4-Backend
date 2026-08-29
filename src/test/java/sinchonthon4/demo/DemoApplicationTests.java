package sinchonthon4.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import sinchonthon4.demo.support.JwtTestProperties;

@SpringBootTest
class DemoApplicationTests {

	@DynamicPropertySource
	static void jwtProperties(DynamicPropertyRegistry registry) {
		JwtTestProperties.register(registry);
	}

	@Test
	void contextLoads() {
	}

}
