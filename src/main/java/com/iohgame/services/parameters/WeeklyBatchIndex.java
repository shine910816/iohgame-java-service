package com.iohgame.services.parameters;

import com.iohgame.framework.utility.parameters.property.Parameters;

public enum WeeklyBatchIndex implements Parameters
{
    WEEKLY_SUN("0"),

    WEEKLY_MON("1"),

    WEEKLY_TUE("2"),

    WEEKLY_WED("3"),

    WEEKLY_THU("4"),

    WEEKLY_FRI("5"),

    WEEKLY_SAT("6"),

    UNKNOWN("");

    private String m_val;

    private WeeklyBatchIndex(String val)
    {
        m_val = val;
    }

    @Override
    public String val()
    {
        return m_val;
    }

    @Override
    public Parameters unknown()
    {
        return UNKNOWN;
    }
}
