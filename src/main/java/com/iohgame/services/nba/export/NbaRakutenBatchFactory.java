package com.iohgame.services.nba.export;

import com.iohgame.framework.connect.base.ConnectBase;
import com.iohgame.framework.utility.ServiceFactory;
import com.iohgame.framework.utility.parameters.property.Action;
import com.iohgame.framework.utility.parameters.property.OptionElement;
import com.iohgame.service.nba.export.NbaRakutenConnect;

public class NbaRakutenBatchFactory extends ServiceFactory
{
    public static NbaRakutenBatchFactory getInstance()
    {
        return new NbaRakutenBatchFactory();
    }

    @Override
    public Action getAction(OptionElement page)
    {
        Action act = null;
        ConnectBase connect = null;
        switch ((NbaRakutenBatchOption) page)
        {
            case NBA_RAKUTEN_WEEKLY_REPORT:
                connect = new NbaRakutenConnect();
                act = new NbaRakutenWeeklyReportExportAction((NbaRakutenConnect) connect);
                break;
            default:
                LOG.error("Batch option is nou found by " + page);
                break;
        }
        return act;
    }

    public enum NbaRakutenBatchOption implements OptionElement
    {
        NBA_RAKUTEN_WEEKLY_REPORT;

        @Override
        public String val()
        {
            return name();
        }
    }
}
