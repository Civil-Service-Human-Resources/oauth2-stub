package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.domain.User;
import uk.gov.cshr.dto.UserDTO;
import uk.gov.cshr.service.AccessTokenService;

@RestController
public class UserController {

    @Autowired
    private AccessTokenService accessTokenService;

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public UserDTO getUserDetailsfromAccessToken(@RequestParam("access_token") String accessToken) {
        User user = accessTokenService.findActiveAccessToken(accessToken).getUser();
        return new UserDTO(user.getEmail(), user.getUid(), user.getRoles());
    }
}
