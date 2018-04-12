package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.cshr.repository.IdentityRepository;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class IdentityControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private IdentityController controller;

    @Mock
    private IdentityRepository identityRepository;

    @Before
    public void setup() {
        initMocks(this);
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    public void shouldReturnNotFoundForUnknownCourse() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/identity")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

//    @Test
//    public void shouldReturnCourse() throws Exception {
//
//        Course course = createCourse();
//
//        when(identityRepository.findById("1"))
//                .thenReturn(Optional.of(course));
//
//        mockMvc.perform(
//                MockMvcRequestBuilders.get("/courses/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title", equalTo("title")));
//    }
}
