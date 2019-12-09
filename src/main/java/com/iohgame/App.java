package com.iohgame;

import com.iohgame.services.nba.export.NbaRakutenBatchFactory;
import com.iohgame.services.nba.export.NbaRakutenBatchFactory.NbaRakutenBatchOption;

public class App 
{
    public static void main( String[] args )
    {
        NbaRakutenBatchFactory.getInstance().getAction(NbaRakutenBatchOption.NBA_RAKUTEN_WEEKLY_REPORT).doMainExecute();
    }
}
