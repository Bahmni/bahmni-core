insert ignore into privilege values ('app:clinical:treatmentTab', 'View Treatment tab', UUID());
insert ignore into privilege values ('app:clinical:ordersTab', 'View Orders tab', UUID());
insert ignore into privilege values ('app:clinical:bacteriologyTab', 'View Bacteriology tab', UUID());
insert ignore into privilege values ('app:implementer-interface', 'Will give access to implementer interface app', UUID());
insert ignore into privilege values ('app:radiology-upload', 'Will give access to radiology app', UUID());
insert ignore into privilege values ('app:patient-documents', 'Will give access to patient documents app', UUID());

# Create Bahmni-App-User-Login role
insert ignore into role values('Bahmni-App-User-Login', 'Will give ability to login to the application', UUID());
insert ignore into role_privilege values('Bahmni-App-User-Login', 'Edit Users');
insert ignore into role_privilege values('Bahmni-App-User-Login', 'Get Providers');
insert ignore into role_privilege values('Bahmni-App-User-Login', 'Get Users');

# Create Registration-App-Read-Only role
insert ignore into role values('Registration-App-Read-Only', 'Will have read only access for Registration app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'Registration-App-Read-Only');
insert ignore into role_privilege values('Registration-App-Read-Only', 'app:registration');
insert ignore into role_privilege values('Registration-App-Read-Only', 'Get Concepts');
insert ignore into role_privilege values('Registration-App-Read-Only', 'Get Encounters');
insert ignore into role_privilege values('Registration-App-Read-Only', 'Get Patients');
insert ignore into role_privilege values('Registration-App-Read-Only', 'Get People');
insert ignore into role_privilege values('Registration-App-Read-Only', 'Get Person Attribute Types');
insert ignore into role_privilege values('Registration-App-Read-Only', 'Get Visit Types');
insert ignore into role_privilege values('Registration-App-Read-Only', 'Get Visits');
insert ignore into role_privilege values('Registration-App-Read-Only', 'View Patients');
insert ignore into role_privilege values('Registration-App-Read-Only', 'Get Observations');

# Create Registration-App role
insert ignore into role values('Registration-App', 'Will have full access for Registration app', UUID());
insert ignore into role_role values('Registration-App-Read-Only', 'Registration-App');
insert ignore into role_privilege values('Registration-App', 'Add Encounters');
insert ignore into role_privilege values('Registration-App', 'Add Patients');
insert ignore into role_privilege values('Registration-App', 'Add Visits');
insert ignore into role_privilege values('Registration-App', 'Edit Encounters');
insert ignore into role_privilege values('Registration-App', 'Edit Patients');
insert ignore into role_privilege values('Registration-App', 'Edit Visits');
insert ignore into role_privilege values('Registration-App', 'Get Encounter Roles');
insert ignore into role_privilege values('Registration-App', 'Get Patient Identifiers');
insert ignore into role_privilege values('Registration-App', 'Get Visit Attribute Types');
insert ignore into role_privilege values('Registration-App', 'Edit Patient Identifiers');
insert ignore into role_privilege values('Registration-App', 'Edit Relationships');
insert ignore into role_privilege values('Registration-App', 'Add Relationships');

# Create Programs-App role
insert ignore into role values('Programs-App', 'Will have full access for Programs app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'Programs-App');
insert ignore into role_privilege values('Programs-App', 'Add Patient Programs');
insert ignore into role_privilege values('Programs-App', 'app:clinical');
insert ignore into role_privilege values('Programs-App', 'app:clinical:locationpicker');
insert ignore into role_privilege values('Programs-App', 'app:clinical:onbehalf');
insert ignore into role_privilege values('Programs-App', 'app:clinical:retrospective');
insert ignore into role_privilege values('Programs-App', 'Edit Patient Programs');
insert ignore into role_privilege values('Programs-App', 'Get Patient Programs');
insert ignore into role_privilege values('Programs-App', 'Get Patients');
insert ignore into role_privilege values('Programs-App', 'Get People');
insert ignore into role_privilege values('Programs-App', 'Get Programs');
insert ignore into role_privilege values('Programs-App', 'Get Visits');
insert ignore into role_privilege values('Programs-App', 'Manage Program Attribute Types');
insert ignore into role_privilege values('Programs-App', 'Purge Program Attribute Types');
insert ignore into role_privilege values('Programs-App', 'View Patient Programs');
insert ignore into role_privilege values('Programs-App', 'View Program Attribute Types');
insert ignore into role_privilege values('Programs-App', 'Get Concepts');
insert ignore into role_privilege values('Programs-App', 'Get Visit Types');

# Create Reports-App role
insert ignore into role values('Reports-App', 'Will have full access for Reports app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'Reports-App');
insert ignore into role_privilege values('Reports-App', 'app:reports');
insert ignore into role_privilege values('Reports-App', 'Get Concepts');
insert ignore into role_privilege values('Reports-App', 'Get Visit Types');

# Create OrderFulfillment-App role
insert ignore into role values('OrderFulfillment-App', 'Will have full access for OrdersFulfillment app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'OrderFulfillment-App');
insert ignore into role_privilege values('OrderFulfillment-App', 'Add Encounters');
insert ignore into role_privilege values('OrderFulfillment-App', 'Add Visits');
insert ignore into role_privilege values('OrderFulfillment-App', 'app:orders');
insert ignore into role_privilege values('OrderFulfillment-App', 'Edit Encounters');
insert ignore into role_privilege values('OrderFulfillment-App', 'Edit Visits');
insert ignore into role_privilege values('OrderFulfillment-App', 'Get Concepts');
insert ignore into role_privilege values('OrderFulfillment-App', 'Get Encounter Roles');
insert ignore into role_privilege values('OrderFulfillment-App', 'Get Encounters');
insert ignore into role_privilege values('OrderFulfillment-App', 'Get Orders');
insert ignore into role_privilege values('OrderFulfillment-App', 'Get Patients');
insert ignore into role_privilege values('OrderFulfillment-App', 'Get Visit Attribute Types');
insert ignore into role_privilege values('OrderFulfillment-App', 'Get Visit Types');
insert ignore into role_privilege values('OrderFulfillment-App', 'Get Visits');

# Create PatientDocuments-App role
insert ignore into role values('PatientDocuments-App', 'Will have full access for Patient Documents app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'PatientDocuments-App');
insert ignore into role_privilege values('PatientDocuments-App', 'Add Visits');
insert ignore into role_privilege values('PatientDocuments-App', 'app:patient-documents');
insert ignore into role_privilege values('PatientDocuments-App', 'Edit Visits');
insert ignore into role_privilege values('PatientDocuments-App', 'Get Concepts');
insert ignore into role_privilege values('PatientDocuments-App', 'Get Encounter Roles');
insert ignore into role_privilege values('PatientDocuments-App', 'Get Encounters');
insert ignore into role_privilege values('PatientDocuments-App', 'Get Patients');
insert ignore into role_privilege values('PatientDocuments-App', 'Get Visit Attribute Types');
insert ignore into role_privilege values('PatientDocuments-App', 'Get Visit Types');
insert ignore into role_privilege values('PatientDocuments-App', 'Get Visits');

# Create Radiology-App role
insert ignore into role values('Radiology-App', 'Will have full access for Radiology app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'Radiology-App');
insert ignore into role_privilege values('Radiology-App', 'Add Visits');
insert ignore into role_privilege values('Radiology-App', 'app:radiology-upload');
insert ignore into role_privilege values('Radiology-App', 'Edit Visits');
insert ignore into role_privilege values('Radiology-App', 'Get Encounter Roles');
insert ignore into role_privilege values('Radiology-App', 'Get Encounters');
insert ignore into role_privilege values('Radiology-App', 'Get Patients');
insert ignore into role_privilege values('Radiology-App', 'Get Visit Attribute Types');
insert ignore into role_privilege values('Radiology-App', 'Get Visits');
insert ignore into role_privilege values('Radiology-App', 'Get Visit Types');
insert ignore into role_privilege values('Radiology-App', 'Get Concepts');

# Create InPatient-App-Read-Only role
insert ignore into role values('InPatient-App-Read-Only', 'Will have read only access for InPatient app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'InPatient-App-Read-Only');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'app:adt');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'Get Admission Locations');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'Get Beds');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'Get Concepts');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'Get Visit Types');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'Get Observations');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'Get Visits');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'Get People');
insert ignore into role_privilege values('InPatient-App-Read-Only', 'Get Patients');

# Create Admin-App role
insert ignore into role values('Admin-App', 'Will have full access for Admin app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'Admin-App');
insert ignore into role_privilege values('Admin-App', 'Add Encounters');
insert ignore into role_privilege values('Admin-App', 'Add Orders');
insert ignore into role_privilege values('Admin-App', 'Add Patient Programs');
insert ignore into role_privilege values('Admin-App', 'Add Patients');
insert ignore into role_privilege values('Admin-App', 'Add Relationships');
insert ignore into role_privilege values('Admin-App', 'Add Visits');
insert ignore into role_privilege values('Admin-App', 'app:admin');
insert ignore into role_privilege values('Admin-App', 'Manage Order Sets');
insert ignore into role_privilege values('Admin-App', 'Edit Encounters');
insert ignore into role_privilege values('Admin-App', 'Edit Orders');
insert ignore into role_privilege values('Admin-App', 'Edit Patient Programs');
insert ignore into role_privilege values('Admin-App', 'Edit Patients');
insert ignore into role_privilege values('Admin-App', 'Edit Relationships');
insert ignore into role_privilege values('Admin-App', 'Edit Visits');
insert ignore into role_privilege values('Admin-App', 'Get Care Settings');
insert ignore into role_privilege values('Admin-App', 'Get Concept Reference Terms');
insert ignore into role_privilege values('Admin-App', 'Get Concepts');
insert ignore into role_privilege values('Admin-App', 'Get Encounter Roles');
insert ignore into role_privilege values('Admin-App', 'Get Encounters');
insert ignore into role_privilege values('Admin-App', 'Get Observations');
insert ignore into role_privilege values('Admin-App', 'Get Order Frequencies');
insert ignore into role_privilege values('Admin-App', 'Get Order Sets');
insert ignore into role_privilege values('Admin-App', 'Get Patient Programs');
insert ignore into role_privilege values('Admin-App', 'Get Patients');
insert ignore into role_privilege values('Admin-App', 'Get Programs');
insert ignore into role_privilege values('Admin-App', 'Get Visit Attribute Types');
insert ignore into role_privilege values('Admin-App', 'Get Visit Types');
insert ignore into role_privilege values('Admin-App', 'Get Visits');
insert ignore into role_privilege values('Admin-App', 'Manage Concept Reference Terms');
insert ignore into role_privilege values('Admin-App', 'Manage Concepts');

# Create InPatient-App role
insert ignore into role values('InPatient-App', 'Will have full access for InPatient app', UUID());
insert ignore into role_role values('InPatient-App-Read-Only', 'InPatient-App');
insert ignore into role_privilege values('InPatient-App', 'Add Encounters');
insert ignore into role_privilege values('InPatient-App', 'Add Visits');
insert ignore into role_privilege values('InPatient-App', 'Assign Beds');
insert ignore into role_privilege values('InPatient-App', 'Edit Admission Locations');
insert ignore into role_privilege values('InPatient-App', 'Edit Encounters');
insert ignore into role_privilege values('InPatient-App', 'Edit Visits');
insert ignore into role_privilege values('InPatient-App', 'Get Encounter Roles');
insert ignore into role_privilege values('InPatient-App', 'Get Encounters');
insert ignore into role_privilege values('InPatient-App', 'Get Observations');
insert ignore into role_privilege values('InPatient-App', 'Get Patients');
insert ignore into role_privilege values('InPatient-App', 'Get People');
insert ignore into role_privilege values('InPatient-App', 'Get Visit Attribute Types');
insert ignore into role_privilege values('InPatient-App', 'Get Visits');

# Create Clinical-App-Common role
insert ignore into role values('Clinical-App-Common', 'Will have common privileges used by other Clinical roles, not be assigned User directly', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'Clinical-App-Common');
insert ignore into role_privilege values('Clinical-App-Common', 'app:clinical');
insert ignore into role_privilege values('Clinical-App-Common', 'app:clinical:locationpicker');
insert ignore into role_privilege values('Clinical-App-Common', 'app:clinical:onbehalf');
insert ignore into role_privilege values('Clinical-App-Common', 'app:clinical:retrospective');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Admission Locations');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Beds');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Care Settings');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Concept Sources');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Concepts');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Encounters');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Observations');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Order Frequencies');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Order Types');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Orders');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Patient Programs');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Patients');
insert ignore into role_privilege values('Clinical-App-Common', 'Get People');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Privileges');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Visit Types');
insert ignore into role_privilege values('Clinical-App-Common', 'Get Visits');
insert ignore into role_privilege values('Clinical-App-Common', 'View Concepts');
insert ignore into role_privilege values('Clinical-App-Common', 'View Encounters');
insert ignore into role_privilege values('Clinical-App-Common', 'View Observations');
insert ignore into role_privilege values('Clinical-App-Common', 'View Order Types');
insert ignore into role_privilege values('Clinical-App-Common', 'View Orders');
insert ignore into role_privilege values('Clinical-App-Common', 'View Patient Programs');
insert ignore into role_privilege values('Clinical-App-Common', 'View Patients');
insert ignore into role_privilege values('Clinical-App-Common', 'View Program Attribute Types');
insert ignore into role_privilege values('Clinical-App-Common', 'View Providers');
insert ignore into role_privilege values('Clinical-App-Common', 'View Users');
insert ignore into role_privilege values('Clinical-App-Common', 'View Visit Types');
insert ignore into role_privilege values('Clinical-App-Common', 'View Visits');
insert ignore into role_privilege values('Clinical-App-Common', 'app:clinical:history');

# Create Clinical-App-Read-Only role
insert ignore into role values('Clinical-App-Read-Only', 'Will have read only access to Clinical app', UUID());
insert ignore into role_role values('Clinical-App-Common', 'Clinical-App-Read-Only');
insert ignore into role_privilege values('Clinical-App-Read-Only', 'app:clinical:bacteriologyTab');
insert ignore into role_privilege values('Clinical-App-Read-Only', 'app:clinical:consultationTab');
insert ignore into role_privilege values('Clinical-App-Read-Only', 'app:clinical:diagnosisTab');
insert ignore into role_privilege values('Clinical-App-Read-Only', 'app:clinical:dispositionTab');
insert ignore into role_privilege values('Clinical-App-Read-Only', 'app:clinical:history');
insert ignore into role_privilege values('Clinical-App-Read-Only', 'app:clinical:observationTab');
insert ignore into role_privilege values('Clinical-App-Read-Only', 'app:clinical:ordersTab');
insert ignore into role_privilege values('Clinical-App-Read-Only', 'app:clinical:treatmentTab');

# Create Clinical-App-Save role
insert ignore into role values('Clinical-App-Save', 'Will give ability to save in Clinical app', UUID());
insert ignore into role_privilege values('Clinical-App-Save', 'Add Encounters');
insert ignore into role_privilege values('Clinical-App-Save', 'Add Visits');
insert ignore into role_privilege values('Clinical-App-Save', 'Edit Encounters');
insert ignore into role_privilege values('Clinical-App-Save', 'Edit Visits');
insert ignore into role_privilege values('Clinical-App-Save', 'Get Encounter Roles');
insert ignore into role_privilege values('Clinical-App-Save', 'Get Visit Attribute Types');
insert ignore into role_privilege values('Clinical-App-Save', 'Add Orders');
insert ignore into role_privilege values('Clinical-App-Save', 'Edit Orders');

# Create Clinical-App-Diagnosis role
insert ignore into role values('Clinical-App-Diagnosis', 'Will have full access for Diagnosis tab in Clinical app', UUID());
insert ignore into role_role values('Clinical-App-Save', 'Clinical-App-Diagnosis');
insert ignore into role_role values('Clinical-App-Common', 'Clinical-App-Diagnosis');
insert ignore into role_privilege values('Clinical-App-Diagnosis', 'app:clinical:diagnosisTab');

# Create Clinical-App-Disposition role
insert ignore into role values('Clinical-App-Disposition', 'Will have full access for Disposition tab in Clinical app', UUID());
insert ignore into role_role values('Clinical-App-Save', 'Clinical-App-Disposition');
insert ignore into role_role values('Clinical-App-Common', 'Clinical-App-Disposition');
insert ignore into role_privilege values('Clinical-App-Disposition', 'app:clinical:dispositionTab');

# Create Clinical-App-Observations role
insert ignore into role values('Clinical-App-Observations', 'Will have full access for Observations tab in Clinical app', UUID());
insert ignore into role_role values('Clinical-App-Save', 'Clinical-App-Observations');
insert ignore into role_role values('Clinical-App-Common', 'Clinical-App-Observations');
insert ignore into role_privilege values('Clinical-App-Observations', 'app:clinical:observationTab');

# Create Clinical-App-Treatment role
insert ignore into role values('Clinical-App-Treatment', 'Will have full access for Treatment tab in Clinical app', UUID());
insert ignore into role_role values('Clinical-App-Save', 'Clinical-App-Treatment');
insert ignore into role_role values('Clinical-App-Common', 'Clinical-App-Treatment');
insert ignore into role_privilege values('Clinical-App-Treatment', 'app:clinical:treatmentTab');

# Create Clinical-App-Orders role
insert ignore into role values('Clinical-App-Orders', 'Will have full access for Orders tab in Clinical app', UUID());
insert ignore into role_role values('Clinical-App-Save', 'Clinical-App-Orders');
insert ignore into role_role values('Clinical-App-Common', 'Clinical-App-Orders');
insert ignore into role_privilege values('Clinical-App-Orders', 'app:clinical:ordersTab');

# Create Clinical-App-Bacteriology role
insert ignore into role values('Clinical-App-Bacteriology', 'Will have full access for Bacteriology tab in Clinical app', UUID());
insert ignore into role_role values('Clinical-App-Save', 'Clinical-App-Bacteriology');
insert ignore into role_role values('Clinical-App-Common', 'Clinical-App-Bacteriology');
insert ignore into role_privilege values('Clinical-App-Bacteriology', 'app:clinical:bacteriologyTab');

# Create Clinical-App role
insert ignore into role values('Clinical-App', 'Will have full access to Clinical app', UUID());
insert ignore into role_role values('Clinical-App-Save', 'Clinical-App');
insert ignore into role_role values('Clinical-App-Read-Only', 'Clinical-App');

# Create Implementer-Interface-App role
insert ignore into role values('Implementer-Interface-App', 'Will have full access to Implementer Interface app', UUID());
insert ignore into role_role values('Bahmni-App-User-Login', 'Implementer-Interface-App');
insert ignore into role_privilege values('Implementer-Interface-App', 'app:implementer-interface');

# Create Bahmni-App role
insert ignore into role values('Bahmni-App', 'Will have full access to Bahmni', UUID());
insert ignore into role_role values('Registration-App', 'Bahmni-App');
insert ignore into role_role values('Programs-App', 'Bahmni-App');
insert ignore into role_role values('Reports-App', 'Bahmni-App');
insert ignore into role_role values('OrderFulfillment-App', 'Bahmni-App');
insert ignore into role_role values('PatientDocuments-App', 'Bahmni-App');
insert ignore into role_role values('Radiology-App', 'Bahmni-App');
insert ignore into role_role values('Implementer-Interface-App', 'Bahmni-App');
insert ignore into role_role values('Admin-App', 'Bahmni-App');
insert ignore into role_role values('InPatient-App', 'Bahmni-App');
insert ignore into role_role values('Clinical-App', 'Bahmni-App');
# Create SuperAdmin role
insert ignore into role values('SuperAdmin', 'Will give full acess to Bahmni and OpenMRS', UUID());
insert ignore into role_privilege select 'SuperAdmin',privilege from privilege;