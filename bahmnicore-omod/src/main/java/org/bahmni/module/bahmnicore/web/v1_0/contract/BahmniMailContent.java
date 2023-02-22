package org.bahmni.module.bahmnicore.web.v1_0.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BahmniMailContent {
    private String pdf;
    private String fileName;
    private String subject;
    private String body;
    private String[] cc;
    private String[] bcc;
}
