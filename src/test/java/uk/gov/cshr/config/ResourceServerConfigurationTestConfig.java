package uk.gov.cshr.config;

import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

public class ResourceServerConfigurationTestConfig extends ResourceServerConfigurerAdapter {

    /* No support for OAuth in Spring tests basically.
     * Avoid unauthorised errors by overriding the stateless field
     *
     * The mocked Security Context in spring unit tests is naturally
     * not based on an OAuth2 Access Token and the Authorization Header is not present in the request.
     *
     * See the following web page for more details
     * https://github.com/spring-projects/spring-security-oauth/issues/385
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.stateless(false);
    }

}