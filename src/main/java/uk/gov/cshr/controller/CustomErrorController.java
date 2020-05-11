package uk.gov.cshr.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.boot.web.servlet.error.ErrorController;
import uk.gov.cshr.controller.utils.ErrorPageMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {
  private static final String GENERIC_ERROR = "/error";

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

    if (status != null) {
      Integer statusCode = Integer.valueOf(status.toString());
        return ErrorPageMap.ERROR_PAGES
                .getOrDefault(statusCode, GENERIC_ERROR);
    }
    return GENERIC_ERROR;
  }

  @Override
  public String getErrorPath() {
    return GENERIC_ERROR;
  }
}
