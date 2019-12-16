package com.iohgame.services.parameters;

import com.iohgame.framework.utility.parameters.property.Parameters;

public enum MonthlyBatchIndex implements Parameters
{
    MONTHLY_01("1"),

    MONTHLY_02("2"),

    MONTHLY_03("3"),

    MONTHLY_04("4"),

    MONTHLY_05("5"),

    MONTHLY_06("6"),

    MONTHLY_07("7"),

    MONTHLY_08("8"),

    MONTHLY_09("9"),

    MONTHLY_10("10"),

    MONTHLY_11("11"),

    MONTHLY_12("12"),

    MONTHLY_13("13"),

    MONTHLY_14("14"),

    MONTHLY_15("15"),

    MONTHLY_16("16"),

    MONTHLY_17("17"),

    MONTHLY_18("18"),

    MONTHLY_19("19"),

    MONTHLY_20("20"),

    MONTHLY_21("21"),

    MONTHLY_22("22"),

    MONTHLY_23("23"),

    MONTHLY_24("24"),

    MONTHLY_25("25"),

    MONTHLY_26("26"),

    MONTHLY_27("27"),

    MONTHLY_28("28"),

    UNKNOWN("");

    private String m_val;

    private MonthlyBatchIndex(String val)
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
