package com.iohgame.services;

import com.iohgame.framework.connect.base.ConnectBase;
import com.iohgame.framework.utility.ServiceFactory;
import com.iohgame.framework.utility.parameters.property.Action;
import com.iohgame.framework.utility.parameters.property.OptionElement;
import com.iohgame.service.nba.export.NbaRakutenConnect;
import com.iohgame.service.nba.synch.NbaImpl;
import com.iohgame.services.custom.CustomSendBirthMailAction;
import com.iohgame.services.custom.parameters.SendBirthMailImpl;
import com.iohgame.services.nba.NbaReportAction;
import com.iohgame.services.nba.NbaSynchAction;
import com.iohgame.services.parameters.ServicePages;

public class BatchFactory extends ServiceFactory
{
    public static BatchFactory getInstance()
    {
        return new BatchFactory();
    }

    @Override
    public Action getAction(OptionElement page)
    {
        Action act = null;
        ConnectBase connect = null;
        switch ((ServicePages) page)
        {
            case NBA_SYNCHRONIZE:
                connect = new NbaImpl();
                act = new NbaSynchAction((NbaImpl) connect);
                break;
            case BIRTHDAY_PRESENT:
                connect = new SendBirthMailImpl();
                act = new CustomSendBirthMailAction((SendBirthMailImpl) connect);
                break;
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
