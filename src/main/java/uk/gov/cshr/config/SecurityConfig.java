package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.*;
import uk.gov.cshr.repository.ClientRepository;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.TokenRepository;
import uk.gov.cshr.service.security.ClientDetailsService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.service.security.WebSecurityExpressionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${server.managementPort}")
    private int managementPort;

    @Value("${server.port}")
    private int serverPort;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Bean
    public ClientDetailsService clientDetailsService() {
        return new ClientDetailsService(clientRepository);
    }

    @Bean
    public IdentityService identityService() {
        return new IdentityService(identityRepository);
    }

    @Autowired
    public void globalUserDetails(IdentityService identityService, AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(identityService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**")
                .authorizeRequests()
                    .requestMatchers(forPortAndPath(managementPort,"/", "/login", "/webjars/**")).permitAll()
                    .requestMatchers(forPortAndPath(serverPort, "/login", "/webjars/**")).permitAll()
                    .anyRequest().authenticated().and()
                .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/management")
                    .failureUrl("/login?error=true").and()
                .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
    }

    private RequestMatcher forPortAndPath(int port, String... pathPatterns) {
        List<RequestMatcher> requestMatchers = new ArrayList<>();
        for (String pathPattern : pathPatterns) {
            requestMatchers.add(new AntPathRequestMatcher(pathPattern));
        }
        return new AndRequestMatcher(forPort(port), new OrRequestMatcher(requestMatchers));
    }

    private RequestMatcher forPort(final int port) {
        return (HttpServletRequest request) -> port == request.getLocalPort();
    }

    @Override
    public void configure(WebSecurity web) {
        web.expressionHandler(new WebSecurityExpressionHandler());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public TokenStore tokenStore() {
        return new uk.gov.cshr.service.security.TokenStore(tokenRepository);
    }

    @Bean
    @Autowired
    public TokenStoreUserApprovalHandler userApprovalHandler(ClientDetailsService clientDetailsService,
                                                             TokenStore tokenStore) {
        TokenStoreUserApprovalHandler handler = new TokenStoreUserApprovalHandler();
        handler.setTokenStore(tokenStore);
        handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientDetailsService));
        handler.setClientDetailsService(clientDetailsService);
        return handler;
    }

    @Bean
    @Autowired
    public ApprovalStore approvalStore(TokenStore tokenStore) {
        TokenApprovalStore store = new TokenApprovalStore();
        store.setTokenStore(tokenStore);
        return store;
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}
