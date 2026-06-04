package org.bahmni.datamigration.csv;


import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;

public class Patient extends CSVEntity {
    @CSVHeader(name="REG_NO")
    public String registrationNumber;
    @CSVHeader(name="REG_DATE")
    public String registrationDate;
    @CSVHeader(name="FNAME")
    public String firstName;
    @CSVHeader(name="LNAME")
    public String lastName;
    @CSVHeader(name="FHNAME")
    public String fathersName;
    @CSVHeader(name="P_SEX")
    public String sex;
    @CSVHeader(name="P_DOB")
    public String dob;
    @CSVHeader(name="P_AGE")
    public String age;
    @CSVHeader(name="P_HEIGHT")
    public String height;
    @CSVHeader(name="P_WEIGHT")
    public String weight;
    @CSVHeader(name="VILLAGE")
    public String village;
    @CSVHeader(name="CITY")
    public String city;
    @CSVHeader(name="P_POST")
    public String postOffice;
    @CSVHeader(name="EDUCATION")
    public String education;
    @CSVHeader(name="OCCUPATION")
    public String occupation;
    @CSVHeader(name="P_MEMBER")
    public String primaryMember;
    @CSVHeader(name="P_TB")
    public String hasTB;
    @CSVHeader(name="BALANCE_AMT")
    public String balanceAmount;
    @CSVHeader(name="Remark")
    public String remark;
    @CSVHeader(name="FNameID")
    public String fNameId;
    @CSVHeader(name="CasteID")
    public String casteId;
    @CSVHeader(name="FHNameID")
    public String fhNameId;
    @CSVHeader(name="EducationID")
    public String educationId;
    @CSVHeader(name="OccupationID")
    public String occupationId;
    @CSVHeader(name="VillageID")
    public String villageId;
    @CSVHeader(name="TahsilID")
    public String tahsilId;
    @CSVHeader(name="DistrictID")
    public String districtId;
    @CSVHeader(name="TahsilID2")
    public String tahsilId2;
    @CSVHeader(name="VillageID2")
    public String villageId2;
    @CSVHeader(name="Neighborhood")
    public String neighbourhood;
    @CSVHeader(name="GramPanchID")
    public String gramPanchayatId;
    @CSVHeader(name="LNameID")
    public String lastNameId;
    @CSVHeader(name="ClassID")
    public String classId;
    @CSVHeader(name="memberVillageID")
    public String memberVillageId;
    @CSVHeader(name="GramPanch")
    public String gramPanch;
    @CSVHeader(name="Tahsil")
    public String tahsil;

}
