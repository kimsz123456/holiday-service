package com.holiday.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RestTemplateErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = HttpStatus.resolve(response.getStatusCode().value());

        if (statusCode == null) {
            throw new IOException("알 수 없는 HTTP 상태 코드: " + response.getStatusCode().value());
        }

        byte[] body = getResponseBody(response);

        if (statusCode.is4xxClientError()) {
            throw new HttpClientErrorException(
                statusCode,
                "클라이언트 오류: " + statusCode.value(),
                body,
                StandardCharsets.UTF_8
            );
        }

        if (statusCode.is5xxServerError()) {
            throw new HttpServerErrorException(
                statusCode,
                "서버 오류: " + statusCode.value(),
                body,
                StandardCharsets.UTF_8
            );
        }
    }
}
