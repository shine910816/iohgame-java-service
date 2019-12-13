package com.iohgame.services.parameters;

import com.iohgame.framework.utility.parameters.property.OptionElement;

public enum ServicePages implements OptionElement
{
    NBA_REPORT,

    NBA_SYNCHRONIZE,

    BIRTHDAY_PRESENT;

    @Override
    public String val()
    {
        return name();
    }
}
