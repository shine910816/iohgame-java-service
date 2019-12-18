package com.iohgame.services.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.iohgame.framework.utility.BatchAction;
import com.iohgame.framework.utility.ReadFiles;
import com.iohgame.framework.utility.Request;
import com.iohgame.framework.utility.SendMail;
import com.iohgame.framework.utility.Utility;
import com.iohgame.framework.utility.parameters.constant.ConstDatetime;
import com.iohgame.service.custom.CustomInfoDao;
import com.iohgame.service.point.CustomPointHistoryDao;
import com.iohgame.service.point.CustomPointHistoryDao.PointType;
import com.iohgame.services.custom.parameters.SendBirthMailImpl;

public class CustomSendBirthMailAction extends BatchAction<SendBirthMailImpl>
{
    public static final Integer PRESENT_POINT = 100;

    public CustomSendBirthMailAction(SendBirthMailImpl database, Request request)
    {
        super(database, request);
    }

    @Override
    public boolean doMainValidate()
    {
        return true;
    }

    @Override
    public boolean doMainExecute()
    {
        List<CustomInfoDao> sendMailList = new ArrayList<>();
        List<CustomInfoDao> sendMessageList = new ArrayList<>();
        List<CustomInfoDao> presentList = filterByCustomBirthday();
        if (!Utility.isEmpty(presentList))
        {
            for (CustomInfoDao custom : presentList)
            {
                if (custom.customHasMail())
                {
                    sendMailList.add(custom);
                    continue;
                }
                if (custom.customHasTel())
                {
                    sendMessageList.add(custom);
                }
            }
        }
        if (!Utility.isEmpty(presentList))
        {
            for (CustomInfoDao custom : presentList)
            {
                try
                {
                    Integer customId = custom.customId();
                    LOG.info("Present point for custom " + customId);
                    Integer customPoint = connect().point().getCustomPoint(customId).customPoint();
                    connect().point().updateCustomPoint(customId, customPoint + PRESENT_POINT);
                    Map<String, String> params = new HashMap<>();
                    params.put("custom_id", Utility.toString(customId));
                    params.put("point_value", Utility.toString(PRESENT_POINT));
                    params.put("point_type", PointType.BIRTHDAY.val());
                    params.put("point_note", "");
                    params.put("point_before", Utility.toString(customPoint));
                    params.put("point_after", Utility.toString(customPoint + PRESENT_POINT));
                    connect().point().insertPointHistory(params);
                }
                catch (Exception e)
                {
                    LOG.error(e.getMessage());
                    continue;
                }
            }
        }
        if (!Utility.isEmpty(sendMailList))
        {
            SendMail sm = new SendMail();
            ReadFiles rf = ReadFiles.getInstance().getFileContents("birthday_mail");
            try
            {
                for (CustomInfoDao custom : sendMailList)
                {
                    String mailTitle = "生日快乐";
                    Map<String, String> params = new HashMap<>();
                    params.put("mail_title", mailTitle);
                    params.put("custom_nick", custom.customNick());
                    params.put("present_point", Utility.toString(PRESENT_POINT));
                    String mailContent = rf.replace(params);
                    sm.sendHtmlEmail(custom.customMailAddr(), mailTitle, mailContent);
                }
            }
            catch (Exception e)
            {
                LOG.error(e.getMessage());
            }
        }
        if (!Utility.isEmpty(sendMessageList))
        {
            for (CustomInfoDao custom : sendMailList)
            {
                // TODO send message
                custom.customTelNumber();
            }
        }
        return true;
    }

    private List<CustomInfoDao> filterByCustomBirthday()
    {
        List<CustomInfoDao> result = new ArrayList<>();
        Date todayDate = Utility.getCurrentDate();
        Date tomorrowDate = Utility.adjustDate(todayDate, 1);
        String today = Utility.getDate(todayDate, ConstDatetime.DATE_MONTH_DAY_NONE);
        String tomorrow = Utility.getDate(tomorrowDate, ConstDatetime.DATE_MONTH_DAY_NONE);
        for (Entry<Integer, CustomInfoDao> item : connect().custom().selectCustomInfo().entrySet())
        {
            if (item.getValue().customHasConfirm())
            {
                String customBirth = Utility.getDate(item.getValue().customBirth(), ConstDatetime.DATE_MONTH_DAY_NONE);
                if (customBirth.equals(today) || (customBirth.equals("0229") && today.equals("0228") && tomorrow.equals("0301")))
                {
                    if (!checkPointPresented(item.getValue().customId()))
                    {
                        result.add(item.getValue());
                    }
                }
            }
        }
        return result;
    }

    private Boolean checkPointPresented(Integer customId)
    {
        Boolean result = false;
        String thisYear = Utility.getDate(Utility.getCurrentDate(), "yyyy");
        List<CustomPointHistoryDao> customPointHistory = connect().point().getCustomPointHistory(customId);
        if (!Utility.isEmpty(customPointHistory))
        {
            for (CustomPointHistoryDao history : customPointHistory)
            {
                if (history.pointType().equals(PointType.BIRTHDAY))
                {
                    if (Utility.getDate(history.insertDate(), "yyyy").equals(thisYear))
                    {
                        result = true;
                        break;
                    }
                    else
                    {
                        continue;
                    }
                }
                else
                {
                    continue;
                }
            }
        }
        return result;
    }
}
