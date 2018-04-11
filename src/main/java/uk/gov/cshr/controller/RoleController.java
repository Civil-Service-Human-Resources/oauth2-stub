package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.service.RoleService;

@Controller
@RequestMapping("/management")
public class RoleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @GetMapping("/roles")
    public String roles(Model model) {
        LOGGER.debug("Listing all roles");

        Iterable<Role> roles = roleService.findAll();

        model.addAttribute("roles", roles);
        model.addAttribute("role", new Role());

        return "roles";
    }


    @GetMapping("/roles/edit/{id}")
    public String roleEdit(Model model) {
        LOGGER.debug("Editing role new role ${id}");

        Iterable<Role> roles = roleService.findAll();


        return "roles";
    }

    @PostMapping("/roles/create")
    public String roleSubmit(@ModelAttribute("role") Role role) {
        LOGGER.debug("Creating new role {}", role.toString());

        roleService.createNewRole(role);

        return "redirect:/management/roles";
    }

}
