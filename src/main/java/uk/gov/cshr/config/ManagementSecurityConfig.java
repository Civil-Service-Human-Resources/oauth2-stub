package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import uk.gov.cshr.service.security.WebSecurityExpressionHandler;

import static uk.gov.cshr.config.SecurityConfig.forPort;

@Configuration
@Order(100)
public class ManagementSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${server.managementPort}")
    private int managementPort;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatcher(forPort(managementPort))
                .authorizeRequests()
                .antMatchers("/management/login", "/webjars/**", "/assets/**").permitAll()
                .anyRequest().hasAuthority("IDENTITY_MANAGER").and()
                .formLogin()
                .loginPage("/management/login")
                .defaultSuccessUrl("/management/roles")
                .failureUrl("/management/login?error=true").and()
                .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/management/login"));
    }

    @Override
    public void configure(WebSecurity web) {
        web.expressionHandler(new WebSecurityExpressionHandler());
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}
