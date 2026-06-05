package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicoreui.contract.Privilege;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/whoami")
public class WhoamiController {

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody()
    public List<Privilege> getPrivileges(HttpServletResponse response) {
        User authenticatedUser = Context.getUserContext().getAuthenticatedUser();
        if (authenticatedUser != null) {
            Collection<org.openmrs.Privilege> privileges = authenticatedUser.getPrivileges();
            List<Privilege> responsePrivileges = new ArrayList<>();
            for (org.openmrs.Privilege privilege : privileges) {
                responsePrivileges.add(new Privilege(privilege.getName()));
            }
            return responsePrivileges;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return null;
    }
}
