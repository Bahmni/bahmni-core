package org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions;

import org.openmrs.DosingInstructions;

public class FlexibleDosingInstructions extends AbstractDosingInstructions {

    @Override
    protected DosingInstructions createInstance() {
        return new FlexibleDosingInstructions();
    }
}
