package com.iohgame.services.custom.parameters;

import com.iohgame.framework.connect.base.ConnectBase;
import com.iohgame.service.custom.Custom;
import com.iohgame.service.custom.CustomImpl;
import com.iohgame.service.point.CustomPoint;
import com.iohgame.service.point.CustomPointImpl;

public class SendBirthMailImpl extends ConnectBase implements SendBirthMail
{
    private Custom m_custom;
    private CustomPoint m_point;

    public SendBirthMailImpl()
    {
        if (m_custom == null)
        {
            m_custom = new CustomImpl();
        }
        if (m_point == null)
        {
            m_point = new CustomPointImpl();
        }
    }

    @Override
    public Custom custom()
    {
        return m_custom;
    }

    @Override
    public CustomPoint point()
    {
        return m_point;
    }
}
