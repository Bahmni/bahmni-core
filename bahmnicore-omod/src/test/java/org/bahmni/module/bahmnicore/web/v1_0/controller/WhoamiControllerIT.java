package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.bahmni.module.bahmnicoreui.contract.Privilege;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class WhoamiControllerIT extends BaseIntegrationTest {

    @Test
    public void shouldRetrievePrivilegesForAValidSession() throws Exception {
        List<Privilege> privileges = deserialize(handle(
                        newGetRequest("/rest/v1/bahmnicore/whoami")),
                new TypeReference<List<Privilege>>() {
                });
        assertThat(privileges.size(),is(equalTo(0)));
    }


    //Need to add test cases where there is no logged in user
}
