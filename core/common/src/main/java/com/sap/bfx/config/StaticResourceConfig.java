package com.sap.bfx.config;

import com.sap.bfx.utils.CustomHttpServletRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;

@Configuration
@Slf4j
public class StaticResourceConfig {

    @Bean
    public FilterRegistrationBean<StaticResourceFilter> staticResourceFilter() {
        FilterRegistrationBean<StaticResourceFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new StaticResourceFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    public static class StaticResourceFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            if (response instanceof HttpServletResponse res) {
                res.setHeader("Access-Control-Allow-Origin", /*"https://*.ondemand.com"*/ "*");
            }
            if (request instanceof HttpServletRequest) {
                var requestWrapper = new CustomHttpServletRequestWrapper((HttpServletRequest) request);
                requestWrapper.addHeader("Access-Control-Allow-Origin", /*"https://*.ondemand.com"*/ "*");
                request = requestWrapper;
            }

            chain.doFilter(request, response);
        }
    }
}
