package uk.gov.cshr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import uk.gov.cshr.service.security.WebSecurityExpressionHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "identity_api";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServerConfiguration.class);

    @Value("${server.port}")
    private int serverPort;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources
                .resourceId(RESOURCE_ID)
                .stateless(true)
                .expressionHandler(new WebSecurityExpressionHandler());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .anonymous().disable()
                .requestMatchers()
                    .antMatchers("/oauth/resolve", "/oauth/revoke", "/api/**")
                .and()
                    .authorizeRequests()
                        .requestMatchers(request -> serverPort != -1 && request.getLocalPort() != serverPort).denyAll()
                        .antMatchers("/api/**").access("hasAuthority('CLIENT')")
                        .antMatchers("/**").authenticated()
                .and()
                    .exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}
