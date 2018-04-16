package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.AuthenticationDetails;
import uk.gov.cshr.service.RoleService;

import java.util.Optional;


@Controller
@RequestMapping("/management/roles")
public class RoleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationDetails authenticationDetails;

    @GetMapping
    public String roles(Model model) {
        LOGGER.info("Listing all roles");

        Iterable<Role> roles = roleService.findAll();

        model.addAttribute("roles", roles);
        model.addAttribute("role", new Role());

        return "roleList"; // change from 'roles' testrunner produces common infinite loop exception as confuses 'roles' with /roles
    }

    @PostMapping("/create")
    public String roleCreate(@ModelAttribute("role") Role role) {
        LOGGER.info("{} created new role {}", authenticationDetails.getCurrentUsername(), role);

        if (role.getId() == null) {
            roleService.createNewRole(role);
        }

        return "redirect:/management/roles";
    }

    @GetMapping("/update/{id}")
    public String roleUpdate(Model model,
                             @PathVariable("id") long id) {
        LOGGER.info("{} updating role for id {}", authenticationDetails.getCurrentUsername(), id);

        Optional<Role> optionalRole = roleService.getRole(id);

        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();
            model.addAttribute("role", role);
            return "update";
        }

        LOGGER.info("No role found for id {}", id);
        return "redirect:/management/roles";
    }


    @PostMapping("/update")
    public String roleUpdate(@ModelAttribute("role") Role role) {
        roleService.updateRole(role);

        LOGGER.info("{} updated role {}", authenticationDetails.getCurrentUsername(), role);

        return "redirect:/management/roles";
    }

    @GetMapping("/delete/{id}")
    public String roleDelete(Model model,
                             @PathVariable("id") long id) {
        LOGGER.info("{} deleting role for id {}", authenticationDetails.getCurrentUsername(), id);

        Optional<Role> role = roleService.getRole(id);

        if (role.isPresent()) {
            model.addAttribute("role", role.get());
            return "delete";
        }

        LOGGER.info("No role found for id {}", id);
        return "redirect:/management/roles";
    }

    @PostMapping("/delete")
    public String roleDelete(@ModelAttribute("role") Role role) {
        roleRepository.delete(role);

        LOGGER.info("{} deleted role {}", authenticationDetails.getCurrentUsername(), role);

        return "redirect:/management/roles";
    }

    @PostMapping("/roles/edit")
    public String roleUpdate(@ModelAttribute("role") Role role) {
       // role.setRoleId(roleId);
        LOGGER.debug("updated new role {}", role.toString());

        roleService.updateRole(role);

        return "redirect:/management/roles";
    }

}
