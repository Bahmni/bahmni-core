package org.bahmni.module.elisatomfeedclient.api.worker;

import org.ict4h.atomfeed.transaction.AFTransactionWork;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

@ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
@Ignore
public class TransactionMgrIT extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    public void shouldNestTransactionWithSpring() {
        final AtomFeedSpringTransactionManager txMgr = new AtomFeedSpringTransactionManager(transactionManager);
        try {
            txMgr.executeWithTransaction(new AFTransactionWorkWithoutResult() {

                @Override
                public PropagationDefinition getTxPropagationDefinition() {
                    return PropagationDefinition.PROPAGATION_REQUIRES_NEW;
                }

                @Override
                protected void doInTransaction() {
                    try {
                        Connection outerCon1 = txMgr.getConnection();
                        System.out.println("outer connection outer 1st time:" + outerCon1);
                    } catch (Exception e1) {
                        System.out.println("connection fetch outer 1st time :" + e1);
                    }

                    for (int i=1; i <= 2; i++) {
                        System.out.println("********** Exec counter "+i);
                        final AtomicInteger loopCounter = new AtomicInteger(i);
                        try {
                            txMgr.executeWithTransaction(new AFTransactionWork<Object>() {
                                @Override
                                public Object execute() {
                                    try {
                                        if (loopCounter.get() == 2) {
                                            throw new Exception("Throw exception for 2nd iteration");
                                        }
                                        Connection innerCon = txMgr.getConnection();
                                        System.out.println("inner connection:"  + innerCon);
                                    } catch(Exception e2) {
                                        System.out.println("connection fetch inner :" + e2);
                                    }
                                    return null;
                                }
                                public PropagationDefinition getTxPropagationDefinition() {
                                    return PropagationDefinition.PROPAGATION_REQUIRES_NEW;
                                }
                            });
                        } catch (Exception innerTxEx) {
                            System.out.println("********** Exec counter "+ i);
                            System.out.println("inner Tx :" + innerTxEx);
                        }
                    }

                    try {
                        Connection outerCon2 = txMgr.getConnection();
                        System.out.println("outer connection outer 2nd time:" + outerCon2);
                    } catch (Exception e3) {
                        System.out.println("connection fetch outer 2nd time :" + e3);
                    }
                }
            });
        } catch (Exception outerEx) {
            System.out.println("Outer Exception:" + outerEx);
        }
    }

}
