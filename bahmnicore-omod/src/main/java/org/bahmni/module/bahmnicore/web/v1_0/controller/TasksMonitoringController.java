package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.monitoring.response.TasksMonitoringResponse;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.scheduler.TaskDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/scheduledTasks")
public class TasksMonitoringController extends BaseRestController {

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody()
    public List<TasksMonitoringResponse> get() {
        List<TasksMonitoringResponse> allTasks = new ArrayList<>();

        Collection<TaskDefinition> scheduledTasks = Context.getSchedulerService().getScheduledTasks();
        for (TaskDefinition scheduledTask : scheduledTasks) {
            allTasks.add(new TasksMonitoringResponse(scheduledTask.getStarted(),
                    scheduledTask.getTaskClass(),
                    scheduledTask.getLastExecutionTime(),
                    scheduledTask.getNextExecutionTime()
            ));
        }

        return allTasks;
    }


}
