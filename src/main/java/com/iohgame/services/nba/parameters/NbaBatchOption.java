package com.iohgame.services.nba.parameters;

import com.iohgame.framework.utility.parameters.property.OptionElement;

public enum NbaBatchOption implements OptionElement
{
    NBA_RAKUTEN_WEEKLY_REPORT,

    NBA_SYNCHRONIZE;

    @Override
    public String val()
    {
        return name();
    }
}
