package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.service.security.UserDetailsChecker;
import uk.gov.cshr.service.security.WebSecurityExpressionHandler;

import javax.servlet.http.HttpServletResponse;

import static uk.gov.cshr.config.SecurityConfig.forPort;

@Configuration
@Order(101)
public class UserSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    IdentityService identityService;

    @Autowired
    UserDetailsChecker userDetailsChecker;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${server.port}")
    private int serverPort;

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatcher(forPort(serverPort))
                .authorizeRequests()
                .antMatchers("/management/**").denyAll()
                .antMatchers(
                        "/login",
                        "/oauth/logout",
                        "/webjars/**",
                        "/assets/**",
                        "/signup/**",
                        "/reset/**",
                        "/account/passwordUpdated",
                        "/account/reactivate/**",
                        "/account/verify/agency/**").permitAll()
                .anyRequest().authenticated().and()
                .formLogin()
                .loginPage("/login").defaultSuccessUrl(lpgUiUrl)
                .failureHandler(new CustomAuthenticationFailureHandler())
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessHandler((request, response, authentication) -> {
                    String redirectUrl = request.getParameter("returnTo");
                    if (redirectUrl == null) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        response.sendRedirect(redirectUrl);
                    }
                }).and()
                .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
    }

    @Override
    public void configure(WebSecurity web) {
        web.expressionHandler(new WebSecurityExpressionHandler());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPreAuthenticationChecks(userDetailsChecker);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(identityService);
        auth.authenticationProvider(provider);
    }
}
