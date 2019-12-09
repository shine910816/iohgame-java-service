package com.iohgame;

import com.iohgame.services.nba.export.NbaRakutenBatchFactory;
import com.iohgame.services.nba.export.NbaRakutenBatchFactory.BatchOption;

public class App 
{
    public static void main( String[] args )
    {
        NbaRakutenBatchFactory.getInstance().execute(BatchOption.NBA_RAKUTEN_WEEKLY_REPORT);
    }
}
