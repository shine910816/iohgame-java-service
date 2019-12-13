package com.iohgame.services;

import com.iohgame.framework.connect.base.ConnectBase;
import com.iohgame.framework.utility.ServiceFactory;
import com.iohgame.framework.utility.parameters.property.Action;
import com.iohgame.framework.utility.parameters.property.OptionElement;
import com.iohgame.service.nba.export.NbaRakutenConnect;
import com.iohgame.services.nba.NbaReportAction;
import com.iohgame.services.parameters.ServicePages;

public class WeeklyFactory extends ServiceFactory
{
    public static WeeklyFactory getInstance()
    {
        return new WeeklyFactory();
    }

    @Override
    public Action getAction(OptionElement page)
    {
        Action act = null;
        ConnectBase connect = null;
        switch ((ServicePages) page)
        {
            case NBA_REPORT:
                connect = new NbaRakutenConnect();
                act = new NbaReportAction((NbaRakutenConnect) connect);
                break;
            default:
                LOG.error("Batch option is not found by " + page);
                break;
        }
        return act;
    }
}
