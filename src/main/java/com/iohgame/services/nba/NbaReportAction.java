package com.iohgame.services.nba;

import com.iohgame.framework.utility.BatchAction;
import com.iohgame.framework.utility.Request;
import com.iohgame.framework.utility.Utility;
import com.iohgame.framework.utility.parameters.constant.ConstDatetime;
import com.iohgame.service.nba.export.NbaRakutenConnect;
import com.iohgame.service.property.jira.export.parameters.JiraIssueColumns;
import com.iohgame.service.property.jira.export.property.JiraTicketsExport;

public class NbaReportAction extends BatchAction<NbaRakutenConnect>
{
    public NbaReportAction(NbaRakutenConnect connect, Request request)
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
        JiraTicketsExport.getIntance() //
                .createEmptySheet("NBA週報(" + Utility.getCurrentDate(ConstDatetime.DATE_MONTH_DAY_NONE) + ")") //
                .createExportSheet("QA part6", connect().selectPart6TicketsFromTcic(), JiraIssueColumns.KEY, JiraIssueColumns.STATUS, JiraIssueColumns.PRIORITY, JiraIssueColumns.RESOLUTION) //
                .createExportSheet("Bug on NBA PROD", connect().selectBugOnProdTicketsFromTcic(), JiraIssueColumns.KEY, JiraIssueColumns.STATUS, JiraIssueColumns.PRIORITY, JiraIssueColumns.RESOLUTION) //
                .createExportSheet("Not Closed", connect().selectNotClosedTicketsFromTcic(), JiraIssueColumns.KEY, JiraIssueColumns.STATUS) //
                .saveExcelFile("WeeklyReport(" + Utility.getCurrentDate(ConstDatetime.DATE_NONE) + ")");
        return true;
    }
}
