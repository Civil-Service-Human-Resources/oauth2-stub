package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import uk.gov.cshr.service.security.WebSecurityExpressionHandler;

import static uk.gov.cshr.config.SecurityConfig.forPort;
import static uk.gov.cshr.config.SecurityConfig.forPortAndPath;

@Configuration
@Order(101)
public class UserSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${server.port}")
    private int serverPort;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatcher(forPort(serverPort))
                .authorizeRequests()
                    .antMatchers("/management/**").denyAll()
                    .antMatchers("/login", "/webjars/**").permitAll()
                    .anyRequest().authenticated().and()
                .formLogin()
                    .loginPage("/login")
                    .failureUrl("/login?error=true").and()
                .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
    }

    @Override
    public void configure(WebSecurity web) {
        web.expressionHandler(new WebSecurityExpressionHandler());
    }
}
