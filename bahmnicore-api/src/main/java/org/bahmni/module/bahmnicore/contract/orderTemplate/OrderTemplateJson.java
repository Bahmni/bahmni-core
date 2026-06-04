package org.bahmni.module.bahmnicore.contract.orderTemplate;

import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.List;

public class OrderTemplateJson {
        private List<OrderTemplate> orderTemplates;

        public List<OrderTemplate> getOrderTemplates() {
            return orderTemplates;
        }

        public void setOrderTemplates(List<OrderTemplate> orderTemplates) {
            this.orderTemplates = orderTemplates;
        }

        public static class OrderTemplate {
            private String name;
            private List<EncounterTransaction.DrugOrder> drugOrders;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<EncounterTransaction.DrugOrder> getDrugOrders() {
                return drugOrders;
            }

            public void setDrugOrders(List<EncounterTransaction.DrugOrder> drugOrders) {
                this.drugOrders = drugOrders;
            }
        }
    }
