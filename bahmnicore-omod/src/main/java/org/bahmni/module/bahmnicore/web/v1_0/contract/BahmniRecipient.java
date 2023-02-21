package org.bahmni.module.bahmnicore.web.v1_0.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BahmniRecipient {
    private String name;
    private String email;
}
