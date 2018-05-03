package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;

import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ResetRepositoryTest {

    public static final String CODE = "ABC123";
    public static final String EMAIL = "test@example.com";

    @Autowired
    private ResetRepository resetRepository;

    @Test
    public void existsByCodeReturnsCorrectBoolean() {
        Reset reset = createReset();

        resetRepository.save(reset);

        assertThat(resetRepository.existsByCode(CODE), equalTo(true));
        assertThat(resetRepository.existsByCode("def567"), equalTo(false));
    }

    private Reset createReset() {
        Reset reset = new Reset();

        reset.setCode(CODE);
        reset.setEmail(EMAIL);
        reset.setResetStatus(ResetStatus.PENDING);

        return reset;
    }

}