package com.iohgame.services.nba;

import com.iohgame.framework.connect.base.ConnectBase;
import com.iohgame.framework.utility.ServiceFactory;
import com.iohgame.framework.utility.parameters.property.Action;
import com.iohgame.framework.utility.parameters.property.OptionElement;
import com.iohgame.service.nba.export.NbaRakutenConnect;
import com.iohgame.service.nba.synch.NbaImpl;
import com.iohgame.services.nba.parameters.NbaBatchOption;

public class NbaFactory extends ServiceFactory
{
    public static NbaFactory getInstance()
    {
        return new NbaFactory();
    }

    @Override
    public Action getAction(OptionElement page)
    {
        Action act = null;
        ConnectBase connect = null;
        switch ((NbaBatchOption) page)
        {
            case NBA_REPORT:
                connect = new NbaRakutenConnect();
                act = new NbaReportAction((NbaRakutenConnect) connect);
                break;
            case NBA_SYNCHRONIZE:
                connect = new NbaImpl();
                act = new NbaSynchAction((NbaImpl) connect);
                break;
            default:
                LOG.error("Batch option is not found by " + page);
                break;
        }
        return act;
    }
}
