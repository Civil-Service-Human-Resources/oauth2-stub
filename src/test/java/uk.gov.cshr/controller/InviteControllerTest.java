package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.AuthenticationDetails;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InviteControllerTest {

    @InjectMocks
    private InviteController inviteController;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private InviteRepository inviteRepository;

    @Mock
    private AuthenticationDetails authenticationDetails;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(inviteController).build();
    }

    @Test
    public void shouldLoadInviteSuccessfully() throws Exception {
        this.mockMvc.perform(get("/management/invite"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

}