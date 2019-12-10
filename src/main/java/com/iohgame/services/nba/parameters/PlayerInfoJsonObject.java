package com.iohgame.services.nba.parameters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iohgame.framework.utility.MainClass;
import com.iohgame.framework.utility.Utility;

public class PlayerInfoJsonObject extends MainClass implements PlayerInfo
{
    private Integer m_playerId;
    private String m_firstName;
    private String m_lastName;
    private Integer m_teamId;
    private Integer m_teamYear;
    private Integer m_jersey;
    private Integer m_pos1;
    private Integer m_pos2;
    private Float m_height;
    private Float m_weight;
    private String m_birthDate;
    private String m_country;
    private Boolean m_isActive;

    protected PlayerInfoJsonObject(JsonObject json)
    {
        setProperty(json);
    }

    private void setProperty(JsonObject json)
    {
        m_playerId = Utility.toInteger(json.get("personId").getAsString());
        m_isActive = json.get("isActive").getAsBoolean();
        m_firstName = json.get("firstName").getAsString();
        m_lastName = json.get("lastName").getAsString();
        Iterator<JsonElement> teamInfoList = json.get("teams").getAsJsonArray().iterator();
        Integer teamId = 0;
        Integer teamYear = 0;
        while (teamInfoList.hasNext())
        {
            JsonObject teamInfo = teamInfoList.next().getAsJsonObject();
            teamId = Utility.toInteger(teamInfo.get("teamId").getAsString());
            teamYear = Utility.toInteger(teamInfo.get("seasonEnd").getAsString());
        }
        m_teamId = teamId;
        m_teamYear = teamYear;
        if (Utility.isEmpty(json.get("jersey").getAsString()))
        {
            m_jersey = -1;
        }
        else
        {
            m_jersey = Utility.toInteger(json.get("jersey").getAsString());
        }
        String[] posArray = transPosition(json.get("pos").getAsString());
        m_pos1 = Utility.toInteger(posArray[0]);
        m_pos2 = Utility.toInteger(posArray[1]);
        if (Utility.isEmpty(json.get("heightMeters").getAsString()))
        {
            m_height = 0f;
        }
        else
        {
            m_height = Utility.toFloat(json.get("heightMeters").getAsString());
        }
        if (Utility.isEmpty(json.get("weightKilograms").getAsString()))
        {
            m_weight = 0f;
        }
        else
        {
            m_weight = Utility.toFloat(json.get("weightKilograms").getAsString());
        }
        m_birthDate = transBirthday(json.get("dateOfBirthUTC").getAsString());
        m_country = json.get("country").getAsString();
    }

    private String[] transPosition(String param)
    {
        if (Utility.isEmpty(param))
        {
            return "0,0".split(",");
        }
        String[] posArr = param.split("-");
        Map<String, String> except = new HashMap<>();
        except.put("C", "1");
        except.put("F", "2");
        except.put("G", "3");
        String result = except.get(posArr[0]);
        if (posArr.length == 1)
        {
            result += ",0";
        }
        else
        {
            result += "," + except.get(posArr[1]);
        }
        return result.split(",");
    }

    private String transBirthday(String param)
    {
        if (Utility.isEmpty(param))
        {
            return "1900-01-01";
        }
        return param;
    }

    @Override
    public Integer playerId()
    {
        return m_playerId;
    }

    @Override
    public String firstName()
    {
        return m_firstName;
    }

    @Override
    public String lastName()
    {
        return m_lastName;
    }

    @Override
    public Integer teamId()
    {
        return m_teamId;
    }

    @Override
    public Integer teamYear()
    {
        return m_teamYear;
    }

    @Override
    public Integer jersey()
    {
        return m_jersey;
    }

    @Override
    public Integer pos1()
    {
        return m_pos1;
    }

    @Override
    public Integer pos2()
    {
        return m_pos2;
    }

    @Override
    public Float height()
    {
        return m_height;
    }

    @Override
    public Float weight()
    {
        return m_weight;
    }

    @Override
    public String birthDate()
    {
        return m_birthDate;
    }

    @Override
    public String country()
    {
        return m_country;
    }

    @Override
    public Boolean isActive()
    {
        return m_isActive;
    }
}
