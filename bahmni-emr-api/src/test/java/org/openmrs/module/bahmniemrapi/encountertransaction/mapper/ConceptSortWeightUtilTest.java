package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.module.bahmniemrapi.builder.ConceptBuilder;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Locale;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class ConceptSortWeightUtilTest {

    @Before
    public void setUp() throws Exception {
        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
    }

    @Test
    public void shouldComputeSortWeight() throws Exception {
        Concept c1 = new ConceptBuilder().withName("c1").withDataType("N/A").build();
        Concept c11 = new ConceptBuilder().withName("c11").withDataType("N/A").build();
        c1.addSetMember(c11);
        Concept c2 = new ConceptBuilder().withName("c2").withDataType("N/A").build();
        Concept c21 = new ConceptBuilder().withName("c21").withDataType("N/A").build();
        Concept c22 = new ConceptBuilder().withName("c22").withDataType("N/A").build();
        c2.addSetMember(c21);
        c2.addSetMember(c22);

        Assert.assertEquals(1, ConceptSortWeightUtil.getSortWeightFor("c1", Arrays.asList(c1, c2)));
        Assert.assertEquals(2, ConceptSortWeightUtil.getSortWeightFor("c11", Arrays.asList(c1, c2)));
        Assert.assertEquals(3, ConceptSortWeightUtil.getSortWeightFor("c2", Arrays.asList(c1, c2)));
        Assert.assertEquals(4, ConceptSortWeightUtil.getSortWeightFor("c21", Arrays.asList(c1, c2)));
        Assert.assertEquals(5, ConceptSortWeightUtil.getSortWeightFor("c22", Arrays.asList(c1, c2)));
    }

    @Test
    public void shouldReturnZeroSortWeightWhenConceptDoesNotExists() {
        Concept c1 = new ConceptBuilder().withName("c1").withDataType("N/A").build();
        Concept c11 = new ConceptBuilder().withName("c11").withDataType("N/A").build();
        c1.addSetMember(c11);
        Concept c2 = new ConceptBuilder().withName("c2").withDataType("N/A").build();

        Assert.assertEquals(0, ConceptSortWeightUtil.getSortWeightFor("goobe", Arrays.asList(c1, c2)));
    }

}
