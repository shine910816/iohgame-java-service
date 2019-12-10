package com.iohgame.services.nba.parameters;

public interface PlayerInfo
{
    public Integer playerId();

    public String firstName();

    public String lastName();

    public Integer teamId();

    public Integer teamYear();

    public Integer jersey();

    public Integer pos1();

    public Integer pos2();

    public Float height();

    public Float weight();

    public String birthDate();

    public String country();

    public Boolean isActive();
}
