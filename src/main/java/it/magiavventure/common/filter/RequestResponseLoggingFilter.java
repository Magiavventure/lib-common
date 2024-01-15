package it.magiavventure.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        ContentCachingResponseWrapper responseWrapper = responseWrapper(response);

        String requestBody = getRequestBody(httpServletRequest);
        String method = httpServletRequest.getMethod();
        String requestUri = httpServletRequest.getRequestURI();
        logRequest(httpServletRequest, requestBody, method, requestUri);
        BodyCachingRequestWrapper requestWrapper = new BodyCachingRequestWrapper(httpServletRequest, requestBody);

        chain.doFilter(requestWrapper, responseWrapper);

        logResponse(responseWrapper, method, requestUri);
    }
    private void logRequest(HttpServletRequest request, String body, String method, String requestUri) {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"method\":\"")
                .append(method)
                .append("\", ")
                .append("\"url\": \"")
                .append(requestUri)
                .append("\", ")
                .append(headersToString(Collections.list(request.getHeaderNames()), request::getHeader))
                .append("\"body\": ")
                .append(Objects.nonNull(body) && !body.isEmpty() ? body : "{}")
                .append("}");
        log.info("REQUEST -> {}", builder);
    }

    private void logResponse(ContentCachingResponseWrapper response, String method, String requestUri) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"method\":\"")
                .append(method)
                .append("\", ")
                .append("\"url\": \"")
                .append(requestUri)
                .append("\", ")
                .append(headersToString(response.getHeaderNames(), response::getHeader))
                .append("\"status\": ")
                .append(response.getStatus())
                .append(", ")
                .append("\"body\": ")
                .append(new String(response.getContentAsByteArray()))
                .append("}");
        log.info("RESPONSE -> {}", builder);
        response.copyBodyToResponse();
    }

    private String headersToString(Collection<String> headerNames, UnaryOperator<String> headerValueResolver) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"headers\": {");
        AtomicInteger i = new AtomicInteger();
        headerNames.forEach(headerName -> {
            String header = headerValueResolver.apply(headerName);
            builder.append("\"%s\":\"%s\"".formatted(headerName, header)).append(i.get() == (headerNames.size()-1) ? "" : ", ");
            i.getAndIncrement();
        });
        builder.append("}, ");
        return builder.toString();
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader bufferedReader = request.getReader()) {
            return bufferedReader.lines().reduce("", (accumulator, actual) -> accumulator + actual);
        }
    }

    private static class BodyCachingRequestWrapper extends HttpServletRequestWrapper {
        private final String body;

        BodyCachingRequestWrapper(HttpServletRequest request, String body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            return new CachedBodyServletInputStream(bytes);
        }
    }
    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        CachedBodyServletInputStream(byte[] body) {
            this.inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // empty
        }
    }


    private ContentCachingResponseWrapper responseWrapper(ServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        if (response instanceof ContentCachingResponseWrapper responseWrapper) {
            return responseWrapper;
        }
        return new ContentCachingResponseWrapper((HttpServletResponse) response);
    }
}
