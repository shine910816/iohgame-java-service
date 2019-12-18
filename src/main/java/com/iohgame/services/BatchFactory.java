package com.iohgame.services;

import com.iohgame.framework.connect.base.ConnectBase;
import com.iohgame.framework.utility.Request;
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
    public BatchFactory(Request request)
    {
        super(request);
    }

    public static BatchFactory getInstance(Request request)
    {
        return new BatchFactory(request);
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
                act = new NbaSynchAction((NbaImpl) connect, request());
                break;
            case BIRTHDAY_PRESENT:
                connect = new SendBirthMailImpl();
                act = new CustomSendBirthMailAction((SendBirthMailImpl) connect, request());
                break;
            case NBA_REPORT:
                connect = new NbaRakutenConnect();
                act = new NbaReportAction((NbaRakutenConnect) connect, request());
                break;
            default:
                LOG.error("Batch option is not found by " + page);
                break;
        }
        return act;
    }
}
