package uk.gov.cshr.controller;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.cshr.domain.AgencyTokenCapacityUsedDto;
import uk.gov.cshr.service.AgencyTokenCapacityService;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({AgencyController.class})
@WithMockUser(username = "user")
public class AgencyControllerTest {

    private static final String UID = "UID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private AgencyTokenCapacityService agencyTokenCapacityService;

    @Test
    public void getSpacesUsedForAgencyToken() throws Exception {
        AgencyTokenCapacityUsedDto agencyTokenCapacityUsedDto = new AgencyTokenCapacityUsedDto(100L);

        when(agencyTokenCapacityService.getSpacesUsedByAgencyToken(UID)).thenReturn(agencyTokenCapacityUsedDto);

        mockMvc.perform(
                MockMvcRequestBuilders.get(String.format("/agency/%s", UID))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(gson.toJson(agencyTokenCapacityUsedDto)));
    }

    @Test
    public void deleteAgencyToken_callsAgencyTokenCapacityServiceDeleteAgencyTokenOk() throws Exception {
        String agencyTokenUid = UUID.randomUUID().toString();
        mockMvc.perform(
                MockMvcRequestBuilders.delete(String.format("/agency/%s", agencyTokenUid))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(agencyTokenCapacityService, times(1)).deleteAgencyToken(agencyTokenUid);
    }

    @Test
    public void deleteAgencyToken_callsAgencyTokenCapacityServiceDeleteAgencyTokenError() throws Exception {
        String agencyTokenUid = UUID.randomUUID().toString();

        doThrow(Exception.class).when(agencyTokenCapacityService).deleteAgencyToken(agencyTokenUid);

        mockMvc.perform(
                MockMvcRequestBuilders.delete(String.format("/agency/%s", agencyTokenUid))
                        .with(csrf())
        ).andExpect(status().is5xxServerError());

        verify(agencyTokenCapacityService, times(1)).deleteAgencyToken(agencyTokenUid);
    }
}
