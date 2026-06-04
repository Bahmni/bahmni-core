package org.bahmni.module.bahmnicore.forms2.util;

import org.junit.Test;
import org.openmrs.Obs;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormUtilTest {

    @Test
    public void shouldReturnFormNameFromGivenFormFieldPath() {
        assertEquals("FormName", FormUtil.getFormNameFromFieldPath("FormName.1/1-0"));
    }

    @Test
    public void shouldReturnEmptyStringAsFormNameIfGivenFormFieldPathDoesNotHaveFormName() {
        assertEquals("", FormUtil.getFormNameFromFieldPath(".1/1-0"));
    }

    @Test
    public void shouldReturnEmptyStringAsFormNameIfGivenFormFieldPathIsNull() {
        assertEquals("", FormUtil.getFormNameFromFieldPath(null));
    }

    @Test
    public void shouldReturnEmptyStringAsFormNameIfGivenFormFieldPathIsEmpty() {
        assertEquals("", FormUtil.getFormNameFromFieldPath(""));
    }

    @Test
    public void shouldReturnEmptyStringAsFormNameIfGivenFormFieldPathDoesNotHaveDot() {
        assertEquals("", FormUtil.getFormNameFromFieldPath("FormName1/1-0"));
    }

    @Test
    public void shouldReturnFormVersionFromGivenFormFieldPath() {
        assertEquals(2, FormUtil.getFormVersionFromFieldPath("FormName.2/1-0"));
    }

    @Test
    public void shouldReturnFormVersionAsZeroIfGivenFormFieldPathDoesNotHaveVersion() {
        assertEquals(0, FormUtil.getFormVersionFromFieldPath("FormName./1-0"));
    }

    @Test
    public void shouldReturnFormVersionAsZeroIfGivenFormFieldPathIsNull() {
        assertEquals(0, FormUtil.getFormVersionFromFieldPath(null));
    }

    @Test
    public void shouldReturnFormVersionAsZeroIfGivenFormFieldPathIsEmpty() {
        assertEquals(0, FormUtil.getFormVersionFromFieldPath(""));
    }

    @Test
    public void shouldReturnFormVersionAsZeroIfGivenFormFieldPathDoesNotHaveDot() {
        assertEquals(0, FormUtil.getFormVersionFromFieldPath("FormName2/1-0"));
    }

    @Test
    public void shouldReturnFormVersionAsZeroIfGivenFormFieldPathDoesNotHaveSlash() {
        assertEquals(0, FormUtil.getFormVersionFromFieldPath("FormName.21-0"));
    }

    @Test
    public void shouldReturnEmptyObsWhenPassedInObsListIsNull() {
        List<Obs> obs = FormUtil.filterFormBuilderObs(null);
        assertEquals(0, obs.size());
    }

    @Test
    public void shouldReturnEmptyObsWhenPassedInObsListIsEmpty() {
        List<Obs> obs = FormUtil.filterFormBuilderObs(new ArrayList<>());
        assertEquals(0, obs.size());
    }

    @Test
    public void shouldReturnEmptyObsWhenPassedInObsDontHaveFormFieldPath() {
        Obs observation = mock(Obs.class);
        List<Obs> obs = FormUtil.filterFormBuilderObs(singletonList(observation));
        assertEquals(0, obs.size());
    }

    @Test
    public void shouldReturnObsWhichHaveFormFieldPath() {
        Obs observation = mock(Obs.class);
        Obs anotherObservation = mock(Obs.class);
        when(observation.getFormFieldPath()).thenReturn("FormName.1/1-0");

        List<Obs> obs = FormUtil.filterFormBuilderObs(asList(observation, anotherObservation));
        assertEquals(1, obs.size());
        assertEquals(observation, obs.get(0));
    }

    @Test
    public void shouldReturnParentFormFieldPathForGivenFormFieldPath() {
        String expectedFormNameWithVersion = "FormName.1";
        String actualFormNameWithVersion = FormUtil.getParentFormFieldPath("FormName.1/1-0");
        assertEquals(expectedFormNameWithVersion, actualFormNameWithVersion);
    }

    @Test
    public void shouldReturnParentFormFieldPathForGivenThreeLevelFormFieldPath() {
        String expectedFormNameWithVersion = "FormName.1/1-0/2-0";
        String actualFormNameWithVersion = FormUtil.getParentFormFieldPath("FormName.1/1-0/2-0/3-0");
        assertEquals(expectedFormNameWithVersion, actualFormNameWithVersion);
    }

    @Test
    public void shouldReturnFormNameAlongWithVersionIfGivenFormFieldPathDoesNotHaveSlash() {
        String expectedFormNameWithVersion = "";
        String actualFormNameWithVersion = FormUtil.getParentFormFieldPath("FormName.1");
        assertEquals(expectedFormNameWithVersion, actualFormNameWithVersion);
    }

    @Test
    public void shouldReturnEmptyStringWhenFormFieldPathIsNull() {
        String expectedFormNameWithVersion = "";
        String actualFormNameWithVersion = FormUtil.getParentFormFieldPath(null);
        assertEquals(expectedFormNameWithVersion, actualFormNameWithVersion);
    }
}
