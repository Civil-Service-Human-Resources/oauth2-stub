package uk.gov.cshr.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ManagementController {

    @RequestMapping("/management")
    public String management() {
        return "management";
    }
}
