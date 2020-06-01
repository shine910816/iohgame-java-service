package com.iohgame.services.parameters;

import com.iohgame.framework.utility.parameters.property.Parameters;

public enum ServicePages implements Parameters
{
    NBA_REPORT,

    NBA_TODO_REPORT,

    NBA_SYNCHRONIZE,

    BIRTHDAY_PRESENT,

    TEMPLATE,

    UNKNOWN;

    @Override
    public String val()
    {
        return name();
    }

    @Override
    public Parameters unknown()
    {
        return UNKNOWN;
    }
}
