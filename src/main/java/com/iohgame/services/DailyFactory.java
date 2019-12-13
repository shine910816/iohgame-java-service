package com.iohgame.services;

import com.iohgame.framework.connect.base.ConnectBase;
import com.iohgame.framework.utility.ServiceFactory;
import com.iohgame.framework.utility.parameters.property.Action;
import com.iohgame.framework.utility.parameters.property.OptionElement;
import com.iohgame.service.nba.synch.NbaImpl;
import com.iohgame.services.custom.CustomSendBirthMailAction;
import com.iohgame.services.custom.parameters.SendBirthMailImpl;
import com.iohgame.services.nba.NbaSynchAction;
import com.iohgame.services.parameters.ServicePages;

public class DailyFactory extends ServiceFactory
{
    public static DailyFactory getInstance()
    {
        return new DailyFactory();
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
            default:
                LOG.error("Batch option is not found by " + page);
                break;
        }
        return act;
    }
}
