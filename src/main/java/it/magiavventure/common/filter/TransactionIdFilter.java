package it.magiavventure.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionIdFilter extends GenericFilterBean {

    private static final String TRANSACTION_ID = "transactionId";
    public TransactionIdFilter() {
        MDC.put(TRANSACTION_ID, UUID.randomUUID().toString());
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String transactionId = UUID.randomUUID().toString();
        String transactionIdHeader = request.getHeader(TRANSACTION_ID);
        if (Objects.nonNull(transactionIdHeader) && !transactionIdHeader.isEmpty()) {
            transactionId = transactionIdHeader;
        }
        MDC.put(TRANSACTION_ID, transactionId);
        response.setHeader(TRANSACTION_ID, transactionId);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
