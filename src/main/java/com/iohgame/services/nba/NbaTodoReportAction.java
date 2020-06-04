package com.iohgame.services.nba;

import java.util.List;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.iohgame.framework.utility.BatchAction;
import com.iohgame.framework.utility.Request;
import com.iohgame.service.nba.export.NbaRakutenConnect;

public class NbaTodoReportAction extends BatchAction<NbaRakutenConnect>
{
    public NbaTodoReportAction(NbaRakutenConnect connect, Request request)
    {
        super(connect, request);
    }

    @Override
    public boolean doMainValidate()
    {
        return true;
    }

    @Override
    public boolean doMainExecute()
    {
        LOG.info("Select TODO Jira issue");
        List<Issue> issues = connect().selectTodoTickets();
        for (Issue issue : issues)
        {
            LOG.info(issue.getKey());
            LOG.info(issue.getSummary());
        }
        return true;
    }
}
