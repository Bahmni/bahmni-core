UPDATE scheduler_task_config SET 
    schedulable_class="org.bahmni.module.elisatomfeedclient.api.task.OpenElisPatientFeedTask",
    name = "OpenElis Patient Atom Feed Task" 
where schedulable_class = "org.bahmni.module.elisatomfeedclient.api.task.OpenElisAtomFeedTask";

INSERT INTO scheduler_task_config(name, schedulable_class, start_time, start_time_pattern, repeat_interval, start_on_startup, started, created_by, date_created, uuid)
VALUES ('OpenElis Lab Result Atom Feed Task', 'org.bahmni.module.elisatomfeedclient.api.task.OpenElisLabResultFeedTask', now(), 'MM/dd/yyyy HH:mm:ss', 15, 1, 1, 1,  curdate(), uuid());