    package org.bahmni.module.bahmnicore.contract.visit;

    public class BahmniVisitResponse {
        private String uuid;
        private String patientUuid;
        public VisitTypeResponse visitType;
        private String locationUuid;
        private String startDatetime;
        private String stopDatetime;
      
        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getPatientUuid() {
            return patientUuid;
        }

        public void setPatientUuid(String patientUuid) {
            this.patientUuid = patientUuid;
        }

        public VisitTypeResponse getVisitType() {
            return visitType;
        }

        public void setVisitType(VisitTypeResponse visitType) {
            this.visitType = visitType;
        }

        public String getLocationUuid() {
            return locationUuid;
        }

        public void setLocationUuid(String locationUuid) {
            this.locationUuid = locationUuid;
        }

        public String getStartDatetime() {
            return startDatetime;
        }

        public void setStartDatetime(String startDatetime) {
            this.startDatetime = startDatetime;
        }

        public String getStopDatetime() {
            return stopDatetime;
        }

        public void setStopDatetime(String stopDatetime) {
            this.stopDatetime = stopDatetime;
        }
    }

