package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.BahmniOrderSetDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.OrderSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BahmniOrderSetImplIT extends BaseIntegrationTest{
    @Autowired
    private BahmniOrderSetDao bahmniOrderSetDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("orderSet.xml");
    }

    @Test
    public void shouldReturnOrderSetsWhichContainsSearchTermInName() throws Exception {
        List<OrderSet> result = bahmniOrderSetDao.getOrderSetByQuery("Order");
        assertThat(result.size(), is(equalTo(3)));
    }

    @Test
    public void shouldReturnOrderSetsWhichContainsSearchTermInDescription() throws Exception {
        List<OrderSet> result = bahmniOrderSetDao.getOrderSetByQuery("New_Order");
        assertThat(result.size(), is(equalTo(1)));
        Assert.assertEquals("222fb1d0-a666-22e3-dde2-0110211c1111", result.get(0).getUuid());
    }

    @Test
    public void shouldNotAnyResultsIfBothNameAndDescriptionDoNotContainSearchTerm() throws Exception{
        List<OrderSet> result = bahmniOrderSetDao.getOrderSetByQuery("Random");
        assertThat(result.size(), is(equalTo(0)));
    }


}