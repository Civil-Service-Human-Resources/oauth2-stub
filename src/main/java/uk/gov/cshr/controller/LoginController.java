package uk.gov.cshr.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

  @Value("${lpg.uiUrl}")
  private String lpgUiUrl;

  @RequestMapping("/login")
  public String login(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DefaultSavedRequest dsr =
        (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
    if (dsr != null && dsr.getQueryString() == null) {
      response.sendRedirect(lpgUiUrl);
    }
    return "login";
  }

  @RequestMapping("/management/login")
  public String managementLogin() {
    return "management-login";
  }
}
