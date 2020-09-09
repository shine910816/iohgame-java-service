package com.iohgame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.iohgame.framework.utility.MainClass;
import com.iohgame.service.nba.avod.NbaAvodConnect;
import com.iohgame.service.nba.avod.NbaContentDao;
import com.iohgame.service.nba.avod.NbaContentDao.ContentType;

public class Test extends MainClass
{
    public static void main(String[] args)
    {
        Test t = new Test();
        List<NbaContentDao> list = new NbaAvodConnect().set(t.getFileIndexes()).get();
        for (ContentType type : EnumSet.allOf(ContentType.class))
        {
            for (NbaContentDao content : list)
            {
                if (t.check(content, type))
                {
                    System.out.println(content.avod().title() + "\t" + content.contentType().val() + "\t" + content.url() + "\t" + content.durationTime() + "\t" + content.cuepointList());
                }
            }
        }
    }

    @SuppressWarnings("resource")
    public List<String> getFileIndexes()
    {
        List<String> result = new ArrayList<>();
        try
        {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(new File("src/main/resource/mrss.txt")));
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            line = br.readLine();
            while (line != null)
            {
                result.add(line);
                line = br.readLine();
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public boolean check(NbaContentDao dao, ContentType type)
    {
        if (!dao.contentType().equals(type))
        {
            return false;
        }
        return true;
    }
}
