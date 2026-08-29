package sinchonthon4.demo.global.auth;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import sinchonthon4.demo.dto.response.ApiResponse;
import sinchonthon4.demo.global.exception.ErrorCode;
import sinchonthon4.demo.global.exception.ErrorResponse;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private final JsonMapper jsonMapper;

    public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.fail(
                        errorCode.getHttpStatus().value(),
                        errorCode.getMessage(),
                        ErrorResponse.of(errorCode)));
    }
}
