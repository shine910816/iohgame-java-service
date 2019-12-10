package com.iohgame.services.nba.parameters;

import com.google.gson.JsonObject;
import com.iohgame.framework.utility.MainClass;
import com.iohgame.service.nba.synch.PlayerDao.PlayerLeague;

public class PlayerInfoPackage extends MainClass implements PlayerInfo
{
    private PlayerInfo m_standard = null;
    private PlayerInfo m_africa = null;
    private PlayerInfo m_sacramento = null;
    private PlayerInfo m_vegas = null;
    private PlayerInfo m_utah = null;
    private PlayerLeague m_league = null;

    @Override
    public Integer playerId()
    {
        return main().playerId();
    }

    @Override
    public String firstName()
    {
        return main().firstName();
    }

    @Override
    public String lastName()
    {
        return main().lastName();
    }

    @Override
    public Integer teamId()
    {
        return main().teamId();
    }

    @Override
    public Integer teamYear()
    {
        return main().teamYear();
    }

    @Override
    public Integer jersey()
    {
        return main().jersey();
    }

    @Override
    public Integer pos1()
    {
        return main().pos1();
    }

    @Override
    public Integer pos2()
    {
        return main().pos2();
    }

    @Override
    public Float height()
    {
        return main().height();
    }

    @Override
    public Float weight()
    {
        return main().weight();
    }

    @Override
    public String birthDate()
    {
        return main().birthDate();
    }

    @Override
    public String country()
    {
        return main().country();
    }

    @Override
    public Boolean isActive()
    {
        return main().isActive();
    }

    public Boolean isStandard()
    {
        return m_standard != null;
    }

    public Boolean isAfrica()
    {
        return m_africa != null;
    }

    public Boolean isSacramento()
    {
        return m_sacramento != null;
    }

    public Boolean isVegas()
    {
        return m_vegas != null;
    }

    public Boolean isUtah()
    {
        return m_utah != null;
    }

    public void setInfo(PlayerLeague league, JsonObject json)
    {
        LOG.info("Set info " + json + " for " + league);

        PlayerInfo playerInfo = new PlayerInfoJsonObject(json);
        switch (league)
        {
            case STANDARD:
                m_standard = playerInfo;
                break;
            case AFRICA:
                m_africa = playerInfo;
                break;
            case SACRAMENTO:
                m_sacramento = playerInfo;
                break;
            case VEGAS:
                m_vegas = playerInfo;
                break;
            case UTAH:
                m_utah = playerInfo;
                break;
        }
    }

    private PlayerInfo main()
    {
        if (m_league == null)
        {
            Integer teamYear = -1;
            if (isStandard() && m_standard.teamYear() > teamYear)
            {
                teamYear = m_standard.teamYear();
                m_league = PlayerLeague.STANDARD;
            }
            if (isAfrica() && m_africa.teamYear() > teamYear)
            {
                teamYear = m_africa.teamYear();
                m_league = PlayerLeague.AFRICA;
            }
            if (isSacramento() && m_sacramento.teamYear() > teamYear)
            {
                teamYear = m_sacramento.teamYear();
                m_league = PlayerLeague.SACRAMENTO;
            }
            if (isVegas() && m_vegas.teamYear() > teamYear)
            {
                teamYear = m_vegas.teamYear();
                m_league = PlayerLeague.VEGAS;
            }
            if (isUtah() && m_utah.teamYear() > teamYear)
            {
                teamYear = m_utah.teamYear();
                m_league = PlayerLeague.UTAH;
            }
        }
        switch (m_league)
        {
            case AFRICA:
                return m_africa;
            case SACRAMENTO:
                return m_sacramento;
            case VEGAS:
                return m_vegas;
            case UTAH:
                return m_utah;
            default:
            case STANDARD:
                return m_standard;
        }
    }
}
