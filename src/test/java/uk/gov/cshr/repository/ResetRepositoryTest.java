package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;

import javax.transaction.Transactional;

import java.util.Date;

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
        resetRepository.save(createReset());

        assertThat(resetRepository.existsByCode(CODE), equalTo(true));
        assertThat(resetRepository.existsByCode("def567"), equalTo(false));
    }

    @Test
    public void findByCodeShouldReturnCorrectCode() {
        Reset expectedReset = createReset();
        resetRepository.save(expectedReset);
        Reset actualReset = resetRepository.findByCode(CODE);

        assertThat(actualReset.getCode(), equalTo(expectedReset.getCode()));
        assertThat(actualReset.getEmail(), equalTo(expectedReset.getEmail()));
    }

    private Reset createReset() {
        Reset reset = new Reset();
        reset.setCode(CODE);
        reset.setEmail(EMAIL);
        reset.setResetStatus(ResetStatus.PENDING);
        reset.setRequestedAt(new Date());
        return reset;
    }

}