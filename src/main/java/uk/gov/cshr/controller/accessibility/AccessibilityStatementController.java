package uk.gov.cshr.controller.accessibility;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accessibility")
public class AccessibilityStatementController {

    private static final String ACCESSIBILITY_STATEMENT_TEMPLATE = "accessibilityStatement";

    public AccessibilityStatementController() {
    }

    @GetMapping(path = "/statement")
    public String showAccessibilityStatement(Model model) {
        return ACCESSIBILITY_STATEMENT_TEMPLATE;
    }
}
