package uk.gov.cshr.validation.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.repository.WhiteListRepository;
import javax.transaction.Transactional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional

public class WhitelistedValidatorTest {

    @Autowired
    private WhiteListRepository whitelistRepository;

    @Test
    public void existsByDomainReturnsCorrectBoolean() {
        assertThat(whitelistRepository.existsByDomain("aaib.gov.uk"), equalTo(true));
        assertThat(whitelistRepository.existsByDomain("outlook.com"), equalTo(false));
    }
}