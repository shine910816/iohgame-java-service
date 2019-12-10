package com.iohgame;

import com.iohgame.services.nba.NbaFactory;
import com.iohgame.services.nba.parameters.NbaBatchOption;

public class App 
{
    public static void main( String[] args )
    {
        //NbaFactory.getInstance().getAction(NbaBatchOption.NBA_REPORT).doMainExecute();
        NbaFactory.getInstance().getAction(NbaBatchOption.NBA_SYNCHRONIZE).doMainExecute();
    }
}
