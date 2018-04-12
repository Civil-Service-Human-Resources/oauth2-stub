package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.RoleService;

import java.util.Optional;


@Controller
@RequestMapping("/management")
public class RoleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;


    @GetMapping("/roles")
    public String roles(Model model) {
        LOGGER.debug("Listing all roles");

        Iterable<Role> roles = roleService.findAll();

        model.addAttribute("roles", roles);
        model.addAttribute("role", new Role());

        return "roleList"; // change from 'roles' testrunner produces common infinite loop exception as confuses 'roles' with /roles
    }


    @GetMapping("/roles/edit/{id}")
    public String roleEdit(Model model,
        @PathVariable("id") long id) {
        LOGGER.debug("Editing role new role ${id}");

        Optional<Role> optionalRole = roleService.getRole(id);

        if (optionalRole.isPresent()){
             Role role = optionalRole.get();
            model.addAttribute("role", role);
            return "edit";
        }

        // invalid role goto roles page
        return "redirect:/management/roles";
    }

    @PostMapping("/roles/create")
    public String roleSubmit(@ModelAttribute("role") Role role) {
        LOGGER.debug("Creating new role {}", role.toString());

        roleService.createNewRole(role);

        return "redirect:/management/roles";
    }

    @PostMapping("/roles/edit")
    public String roleUpdate(@ModelAttribute("role") Role role) {
       // role.setRoleId(roleId);
        LOGGER.debug("updated new role {}", role.toString());

        roleService.updateRole(role);

        return "redirect:/management/roles";
    }

    @GetMapping("/roles/delete/{id}")
    public String roleDelete(Model model,
                           @PathVariable("id") long id) {
        LOGGER.debug("Deleting role ${id}");

        Optional<Role> role = roleService.getRole(id);
        if (role.isPresent()){
            model.addAttribute("role", role.get());
            System.out.println("got role role {}"+ role.toString());
            return "delete";
        }

        // invalid role goto roles page
        return "redirect:/management/roles";
    }

    @PostMapping("/roles/delete")
    public String roleDelete(@ModelAttribute("role") Role role) {
        // role.setRoleId(roleId);
        LOGGER.debug("updated new role {}", role.toString());

        roleRepository.delete(role);

        return "redirect:/management/roles";
    }

}
