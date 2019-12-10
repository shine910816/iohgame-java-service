package com.iohgame.services.custom.parameters;

import com.iohgame.framework.connect.mysql.parameters.Database;
import com.iohgame.service.custom.Custom;
import com.iohgame.service.point.CustomPoint;

public interface SendBirthMail extends Database
{
    public Custom custom();

    public CustomPoint point();
}
