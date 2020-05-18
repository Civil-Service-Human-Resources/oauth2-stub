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

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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

    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");
    }

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
}
