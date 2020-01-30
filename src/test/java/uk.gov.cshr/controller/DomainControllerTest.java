package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class DomainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentityService identityService;

    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );
    }

    @Test
    public void shouldReturnTrueIfWhitelistedTrue() throws Exception {
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(true);

        mockMvc.perform(
                get("/domain/isWhitelisted").param("domain", "co"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(identityService, times(1)).isWhitelistedDomain(eq("co"));
    }

    @Test
    public void shouldReturnFalseIfWhitelistedFalse() throws Exception {
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(false);

        mockMvc.perform(
                get("/domain/isWhitelisted").param("domain", "co"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(identityService, times(1)).isWhitelistedDomain(eq("co"));
    }

    @Test
    public void shouldReturnFalseIfExceptionThrown() throws Exception {
        when(identityService.isWhitelistedDomain(anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(
                get("/domain/isWhitelisted").param("domain", "co"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(identityService, times(1)).isWhitelistedDomain(eq("co"));
    }

}
