package com.iohgame.services;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.iohgame.framework.utility.MainClass;
import com.iohgame.framework.utility.Request;
import com.iohgame.framework.utility.Utility;
import com.iohgame.framework.utility.parameters.property.Action;
import com.iohgame.framework.utility.parameters.property.Factory;
import com.iohgame.framework.utility.parameters.property.Parameters;
import com.iohgame.services.parameters.ServicePages;

public class Launcher extends MainClass
{
    private Factory m_factory;
    private Set<ServicePages> m_set = new TreeSet<>();

    @SuppressWarnings("unchecked")
    public Launcher ready(String[] args)
    {
        m_factory = BatchFactory.getInstance(new Request(args));
        Map<String, Object> map = (Map<String, Object>) Utility.yamlAnalysis("batch");
        Map<BatchLevel, Object> transMap = new TreeMap<>();
        for (Entry<String, Object> level : map.entrySet())
        {
            BatchLevel levelKey = Utility.getEnum(level.getKey(), BatchLevel.class);
            if (!levelKey.equals(BatchLevel.UNKNOWN))
            {
                transMap.put(levelKey, level.getValue());
            }
        }
        String weeklyTarget = "WEEKLY_" + Utility.getDate(Utility.getCurrentDate(), "E").toUpperCase();
        String monthlyTarget = "MONTHLY_" + Utility.getDate(Utility.getCurrentDate(), "dd");
        String yearlyTarget = "YEARLY_" + Utility.getDate(Utility.getCurrentDate(), "MMdd");
        for (Entry<BatchLevel, Object> level : transMap.entrySet())
        {
            if (level.getValue() != null)
            {
                switch (level.getKey())
                {
                    case YEARLY:
                        for (Entry<String, String[]> yearlyEntry : ((Map<String, String[]>) level.getValue()).entrySet())
                        {
                            if (yearlyEntry.getKey().equals(yearlyTarget))
                            {
                                for (String yearlyListItem : yearlyEntry.getValue())
                                {
                                    m_set.add(Utility.getEnum(yearlyListItem, ServicePages.class));
                                }
                            }
                        }
                        break;
                    case MONTHLY:
                        for (Entry<String, String[]> monthlyEntry : ((Map<String, String[]>) level.getValue()).entrySet())
                        {
                            if (monthlyEntry.getKey().equals(monthlyTarget))
                            {
                                for (String monthlyListItem : monthlyEntry.getValue())
                                {
                                    m_set.add(Utility.getEnum(monthlyListItem, ServicePages.class));
                                }
                            }
                        }
                        break;
                    case WEEKLY:
                        for (Entry<String, String[]> weeklyEntry : ((Map<String, String[]>) level.getValue()).entrySet())
                        {
                            if (weeklyEntry.getKey().equals(weeklyTarget))
                            {
                                for (String weeklyListItem : weeklyEntry.getValue())
                                {
                                    m_set.add(Utility.getEnum(weeklyListItem, ServicePages.class));
                                }
                            }
                        }
                        break;
                    default:
                        for (String dailyListItem : (List<String>) level.getValue())
                        {
                            m_set.add(Utility.getEnum(dailyListItem, ServicePages.class));
                        }
                        break;
                }
            }
        }
        return this;
    }

    public void execute()
    {
        for (ServicePages page : m_set)
        {
            LOG.info("!!! Action execute for " + page + " !!!");

            Action act = m_factory.getAction(page);
            if (!act.doMainValidate())
            {
                LOG.error("Validation has error happened");
                continue;
            }

            if (!act.doMainExecute())
            {
                LOG.error("Execution has error happened");
            }
        }
    }

    public enum BatchLevel implements Parameters
    {
        DAILY,

        WEEKLY,

        MONTHLY,

        YEARLY,

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
}
