package uk.gov.cshr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/management")
public class InvitesController {

    @GetMapping("/invites")
    public String invites() {
        return "default";
    }

}
