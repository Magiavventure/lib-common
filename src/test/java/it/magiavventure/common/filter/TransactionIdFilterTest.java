package it.magiavventure.common.filter;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction id filter tests")
class TransactionIdFilterTest {

    @Test
    @DisplayName("Add new transactionId to MDC and response header")
    void addTransactionIdToMDCAndResponseHeader_ok() throws ServletException, IOException {
        var filter = new TransactionIdFilter();
        var filterChain = new MockFilterChain();
        var servletRequest = new MockHttpServletRequest();
        var servletResponse = new MockHttpServletResponse();

        filter.doFilter(servletRequest, servletResponse, filterChain);

        Assertions.assertNotNull(servletResponse.getHeader("transactionId"));
        Assertions.assertNotNull(MDC.get("transactionId"));
    }

    @Test
    @DisplayName("Add transactionId from header to MDC and response header")
    void addTransactionFromHeaderIdToMDCAndResponseHeader_ok() throws ServletException, IOException {
        var filter = new TransactionIdFilter();
        var filterChain = new MockFilterChain();
        var servletRequest = new MockHttpServletRequest();
        var transactionId = UUID.randomUUID().toString();
        servletRequest.addHeader("transactionId", transactionId);
        var servletResponse = new MockHttpServletResponse();

        filter.doFilter(servletRequest, servletResponse, filterChain);

        Assertions.assertEquals(transactionId, servletResponse.getHeader("transactionId"));
        Assertions.assertEquals(transactionId, MDC.get("transactionId"));
    }

    @Test
    @DisplayName("Add transactionId from header but is empty to MDC and response header")
    void addTransactionFromHeaderButIsEmptyIdToMDCAndResponseHeader_ok() throws ServletException, IOException {
        var filter = new TransactionIdFilter();
        var filterChain = new MockFilterChain();
        var servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("transactionId", "");
        var servletResponse = new MockHttpServletResponse();

        filter.doFilter(servletRequest, servletResponse, filterChain);

        Assertions.assertNotNull(servletResponse.getHeader("transactionId"));
        Assertions.assertNotNull(MDC.get("transactionId"));
    }
}
