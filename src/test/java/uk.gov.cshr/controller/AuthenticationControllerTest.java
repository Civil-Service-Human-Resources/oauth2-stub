package uk.gov.cshr.controller;

import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.cshr.Application;
import uk.gov.cshr.service.NotifyService;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import java.util.Enumeration;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = Application.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Qualifier("notifyServiceImpl")
    @MockBean
    private NotifyService notifyService;
    @Before
    public void overrideFilters() throws IllegalAccessException {
        Filter[] filters = (Filter[]) FieldUtils.readField(mockMvc, "filters", true);
        for (int i = 0; i < filters.length; i++) {
            if (filters[i].getClass().getSimpleName().equals("PatternMappingFilterProxy")) {
                WebRequestTrackingFilter filter = (WebRequestTrackingFilter) FieldUtils.readField(filters[i],
                        "delegate", true);
                FieldUtils.writeField(filter, "appName", "test", true);
                filter.init(new FilterConfig() {
                    @Override
                    public String getFilterName() {
                        return null;
                    }
                    @Override
                    public ServletContext getServletContext() {
                        return null;
                    }
                    @Override
                    public String getInitParameter(String name) {
                        return null;
                    }
                    @Override
                    public Enumeration<String> getInitParameterNames() {
                        return null;
                    }
                });
            }
        }
    }

    @Test
    public void shouldReturnUnauthorisedWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/oauth/resolve"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnDetailsWhenAuthenticated() throws Exception {
        String accessToken = obtainAccessToken("learner@domain.com", "test");

        mockMvc.perform(get("/oauth/resolve")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.username", is("learner@domain.com")));
    }

    @Test
    public void shouldReturnUnauthorisedWhenRevokingInvalidAccessToken() throws Exception {
        String accessToken = "invalid";

        mockMvc.perform(get("/oauth/revoke")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnNoContentWhenRevokingValidAccessToken() throws Exception {
        String accessToken = obtainAccessToken("learner@domain.com", "test");

        mockMvc.perform(get("/oauth/revoke")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldReturnUnauthorisedWhenRevokingRevokedAccessToken() throws Exception {
        String accessToken = obtainAccessToken("learner@domain.com", "test");

        mockMvc.perform(get("/oauth/revoke")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/oauth/revoke")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    private String obtainAccessToken(String username, String password) throws Exception {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", "9fbd4ae2-2db3-44c7-9544-88e80255b56e");
        params.add("client_secret", "test");
        params.add("username", username);
        params.add("password", password);

        ResultActions result = mockMvc.perform(post("/oauth/token")
                .params(params)
                .header("Authorization", "Basic OWZiZDRhZTItMmRiMy00NGM3LTk1NDQtODhlODAyNTViNTZlOnRlc3Q=")
                .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }
}
