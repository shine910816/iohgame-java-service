package com.iohgame;

import com.iohgame.framework.utility.Launcher;
import com.iohgame.services.DailyFactory;
import com.iohgame.services.WeeklyFactory;
import com.iohgame.services.parameters.ServicePages;

public class App
{
    private Launcher m_daily;
    private Launcher m_weekly;

    public void run()
    {
        if (m_daily == null)
        {
            m_daily = new Launcher(DailyFactory.getInstance() //
                    , ServicePages.BIRTHDAY_PRESENT //
                    , ServicePages.NBA_SYNCHRONIZE//
            );
        }
        if (m_weekly == null)
        {
            m_weekly = new Launcher(WeeklyFactory.getInstance() //
                    , ServicePages.NBA_REPORT //
            );
        }
        m_daily.execute();
        m_weekly.execute();
    }

    public static void main(String[] args)
    {
        new App().run();
    }
}
