package com.iohgame.services.nba;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iohgame.framework.utility.BatchAction;
import com.iohgame.framework.utility.JsonAnalyse;
import com.iohgame.framework.utility.Utility;
import com.iohgame.framework.utility.parameters.constant.ConstDatetime;
import com.iohgame.framework.utility.parameters.property.Parameters;
import com.iohgame.service.nba.synch.AverageStats;
import com.iohgame.service.nba.synch.BoxscoreDao;
import com.iohgame.service.nba.synch.NbaImpl;
import com.iohgame.service.nba.synch.PlayerDao;
import com.iohgame.service.nba.synch.PlayerDao.PlayerLeague;
import com.iohgame.service.nba.synch.ScheduleDao;
import com.iohgame.service.nba.synch.StandingsDao;
import com.iohgame.service.nba.synch.StatsDao;
import com.iohgame.service.nba.synch.StatsDao.StatsType;
import com.iohgame.services.nba.parameters.PlayerInfoPackage;

public class NbaSynchAction extends BatchAction<NbaImpl>
{
    private final boolean LAUNCH_STANDINGS = true;
    private final boolean LAUNCH_SCHEDULE = false;
    private final boolean LAUNCH_BOXSCORE = true;
    private final boolean LAUNCH_TOP = true;
    private final boolean LAUNCH_PLAYER = false;

    /**
     * Game season year
     */
    public static final Integer GAME_SEASON = 2019;

    protected NbaSynchAction(NbaImpl connect)
    {
        super(connect);
    }

    @Override
    public boolean doMainValidate()
    {
        return true;
    }

    @Override
    public boolean doMainExecute()
    {
        if (LAUNCH_SCHEDULE)
        {
            scheduleSynch();
        }
        if (LAUNCH_BOXSCORE)
        {
            boxscoreSynch();
        }
        if (LAUNCH_TOP)
        {
            topSynch();
        }
        if (LAUNCH_PLAYER)
        {
            playerSynch();
        }
        if (LAUNCH_STANDINGS)
        {
            standingSynch();
        }

        return true;
    }

    private void scheduleSynch()
    {
        String url = "http://data.nba.net/10s/prod/v1/" + GAME_SEASON + "/schedule.json";
        JsonObject jsonSchedule = JsonAnalyse.getInstance(url).getJsonObject();
        Iterator<JsonElement> jsonScheduleList = jsonSchedule.get("league").getAsJsonObject().get("standard").getAsJsonArray().iterator();
        while (jsonScheduleList.hasNext())
        {
            JsonObject scheduleItem = jsonScheduleList.next().getAsJsonObject();
            Integer jsonGameId = Utility.toInteger(scheduleItem.get("gameId").getAsString().substring(2));
            if (!getExceptionList().contains(jsonGameId))
            {
                if (!scheduleItem.get("startTimeEastern").getAsString().equals(""))
                {
                    Map<String, String> insertItem = new TreeMap<>();
                    String[] gameUrlCode = scheduleItem.get("gameUrlCode").getAsString().split("/");
                    String[] startTimeUTC = getStartTimeString(scheduleItem.get("startTimeUTC").getAsString()).split(" ");
                    insertItem.put("game_id", Utility.toString(jsonGameId));
                    insertItem.put("game_season", Utility.toString(GAME_SEASON));
                    insertItem.put("game_season_stage", scheduleItem.get("seasonStageId").getAsString());
                    insertItem.put("game_date", gameUrlCode[0]);
                    insertItem.put("game_name", gameUrlCode[1]);
                    insertItem.put("game_date_cn", startTimeUTC[0].replace("-", ""));
                    insertItem.put("game_start_date", startTimeUTC[0] + " " + startTimeUTC[1]);
                    insertItem.put("game_arena", Utility.getEnum(scheduleItem.get("hTeam").getAsJsonObject().get("teamId").getAsString(), TeamArena.class).arenaName());
                    insertItem.put("game_home_team", scheduleItem.get("hTeam").getAsJsonObject().get("teamId").getAsString());
                    insertItem.put("game_away_team", scheduleItem.get("vTeam").getAsJsonObject().get("teamId").getAsString());
                    insertItem.put("game_status", scheduleItem.get("statusNum").getAsString());
                    insertItem.put("game_home_score", Utility.isEmpty(scheduleItem.get("hTeam").getAsJsonObject().get("score").getAsString()) ? //
                            "0" : scheduleItem.get("hTeam").getAsJsonObject().get("score").getAsString());
                    insertItem.put("game_away_score", Utility.isEmpty(scheduleItem.get("vTeam").getAsJsonObject().get("score").getAsString()) ? //
                            "0" : scheduleItem.get("vTeam").getAsJsonObject().get("score").getAsString());
                    insertItem.put("game_away_line_score", "");
                    insertItem.put("game_home_line_score", "");
                    insertItem.put("game_synch_flg", "0");
                    connect().schedule().insertSchedule(insertItem);
                }
            }
        }
    }

    private void boxscoreSynch()
    {
        Map<Integer, ScheduleDao> scheduleGameIdList = connect().schedule().selectSchedule(GAME_SEASON);
        Map<String, Integer> gameIdList = connect().boxscore().selectBoxscoreCountByGameSeason(GAME_SEASON);
        Integer currentTime = Utility.toInteger(Utility.getCurrentDate(ConstDatetime.DATE_NONE));
        for (Integer scheduleGameId : scheduleGameIdList.keySet())
        {
            ScheduleDao scheduleInfo = scheduleGameIdList.get(scheduleGameId);
            Map<String, String> updateItem = new TreeMap<>();
            if (scheduleInfo.playDateCn() <= currentTime && !scheduleInfo.isSynch())
            {
                String url = String.format("http://data.nba.net/10s/prod/v1/%s/00%s_boxscore.json", scheduleInfo.playDate(), scheduleInfo.gameId());
                JsonObject jsonBoxscore = JsonAnalyse.getInstance(url).getJsonObject();
                if (jsonBoxscore.get("basicGameData").getAsJsonObject().get("statusNum").getAsString().equals("3"))
                {
                    updateItem.put("game_status", jsonBoxscore.get("basicGameData").getAsJsonObject().get("statusNum").getAsString());
                    String arenaNameJson = jsonBoxscore.get("basicGameData").getAsJsonObject().get("arena").getAsJsonObject().get("name").getAsString();
                    if (!scheduleInfo.arena().equals(arenaNameJson))
                    {
                        updateItem.put("game_arena", arenaNameJson);
                    }
                    updateItem.put("game_home_score", jsonBoxscore.get("basicGameData").getAsJsonObject().get("hTeam").getAsJsonObject().get("score").getAsString());
                    updateItem.put("game_away_score", jsonBoxscore.get("basicGameData").getAsJsonObject().get("vTeam").getAsJsonObject().get("score").getAsString());
                    Iterator<JsonElement> awayLineScoreJson = jsonBoxscore.get("basicGameData").getAsJsonObject().get("vTeam").getAsJsonObject().get("linescore").getAsJsonArray().iterator();
                    Iterator<JsonElement> homeLineScoreJson = jsonBoxscore.get("basicGameData").getAsJsonObject().get("hTeam").getAsJsonObject().get("linescore").getAsJsonArray().iterator();
                    String awayLineScoreJsonText = "";
                    String homeLineScoreJsonText = "";
                    while (awayLineScoreJson.hasNext())
                    {
                        awayLineScoreJsonText += awayLineScoreJson.next().getAsJsonObject().get("score").getAsString();
                        if (awayLineScoreJson.hasNext())
                        {
                            awayLineScoreJsonText += ",";
                        }
                    }
                    while (homeLineScoreJson.hasNext())
                    {
                        homeLineScoreJsonText += homeLineScoreJson.next().getAsJsonObject().get("score").getAsString();
                        if (homeLineScoreJson.hasNext())
                        {
                            homeLineScoreJsonText += ",";
                        }
                    }
                    if (!scheduleInfo.awayLineScore().equals(awayLineScoreJsonText))
                    {
                        updateItem.put("game_away_line_score", awayLineScoreJsonText);
                    }
                    if (!scheduleInfo.awayLineScore().equals(homeLineScoreJsonText))
                    {
                        updateItem.put("game_home_line_score", homeLineScoreJsonText);
                    }
                    updateItem.put("game_synch_flg", "1");
                }
                if (!gameIdList.containsKey(Utility.toString(scheduleGameId)))
                {
                    if (jsonBoxscore.get("basicGameData").getAsJsonObject().get("statusNum").getAsInt() == 3)
                    {
                        Iterator<JsonElement> activePlayersList = jsonBoxscore.get("stats").getAsJsonObject().get("activePlayers").getAsJsonArray().iterator();
                        while (activePlayersList.hasNext())
                        {
                            JsonObject activePlayer = activePlayersList.next().getAsJsonObject();
                            Integer minutes = Utility.transMinuteToSecond(activePlayer.get("min").getAsString());
                            if (minutes > 0)
                            {
                                String[] min = transMinuteSecond(minutes);
                                Map<String, String> insertItem = new TreeMap<>();
                                insertItem.put("game_season", jsonBoxscore.get("basicGameData").getAsJsonObject().get("seasonYear").getAsString());
                                insertItem.put("game_season_stage", jsonBoxscore.get("basicGameData").getAsJsonObject().get("seasonStageId").getAsString());
                                insertItem.put("game_date", jsonBoxscore.get("basicGameData").getAsJsonObject().get("startDateEastern").getAsString());
                                insertItem.put("game_id", jsonBoxscore.get("basicGameData").getAsJsonObject().get("gameId").getAsString().substring(2));
                                insertItem.put("t_id", activePlayer.get("teamId").getAsString());
                                insertItem.put("p_id", activePlayer.get("personId").getAsString());
                                insertItem.put("g_position", getPositionNumber(activePlayer.get("pos").getAsString()));
                                insertItem.put("g_minutes", min[0]);
                                insertItem.put("g_minutes_sec", min[1]);
                                insertItem.put("g_points", activePlayer.get("points").getAsString());
                                insertItem.put("g_field_goals_made", activePlayer.get("fgm").getAsString());
                                insertItem.put("g_field_goals_attempted", activePlayer.get("fga").getAsString());
                                insertItem.put("g_three_points_made", activePlayer.get("tpm").getAsString());
                                insertItem.put("g_three_points_attempted", activePlayer.get("tpa").getAsString());
                                insertItem.put("g_free_throw_made", activePlayer.get("ftm").getAsString());
                                insertItem.put("g_free_throw_attempted", activePlayer.get("fta").getAsString());
                                insertItem.put("g_rebounds", activePlayer.get("totReb").getAsString());
                                insertItem.put("g_offensive_rebounds", activePlayer.get("offReb").getAsString());
                                insertItem.put("g_defensive_rebounds", activePlayer.get("defReb").getAsString());
                                insertItem.put("g_assists", activePlayer.get("assists").getAsString());
                                insertItem.put("g_steals", activePlayer.get("steals").getAsString());
                                insertItem.put("g_blocks", activePlayer.get("blocks").getAsString());
                                insertItem.put("g_personal_fouls", activePlayer.get("pFouls").getAsString());
                                insertItem.put("g_turnovers", activePlayer.get("turnovers").getAsString());
                                insertItem.put("g_plus_minus", activePlayer.get("plusMinus").getAsString());
                                insertItem.put("g_sort", getSort(insertItem));
                                insertItem.put("g_double_double", getDd2(insertItem));
                                insertItem.put("g_triple_double", getTd3(insertItem));
                                insertItem.put("g_eject", getEject(insertItem));
                                connect().boxscore().insertBoxscore(insertItem);
                            }
                        }
                    }
                }
            }
            if (!Utility.isEmpty(updateItem))
            {
                connect().schedule().updateSchedule(updateItem, scheduleGameId);
            }
        }
    }

    private void topSynch()
    {
        List<StatsDao> statsDaoList = connect().stats().selectStats();
        for (StatsDao statsDao : statsDaoList)
        {
            if (statsDao.statsType().equals(StatsType.TEAM))
            {
                Float m_ppg = 0f;
                Float m_rpg = 0f;
                Float m_apg = 0f;
                Float m_spg = 0f;
                Float m_bpg = 0f;
                Float m_fgp = 0f;
                Float m_tpp = 0f;
                Float m_ftp = 0f;
                Float m_opg = 0f;
                Float m_dpg = 0f;
                Float m_tpg = 0f;
                Float m_fpg = 0f;
                Map<Integer, BoxscoreDao> teamStatsInfo = connect().boxscore().selectTeamStats(statsDao.season(), statsDao.seasonStage());
                for (Integer teamId : teamStatsInfo.keySet())
                {
                    AverageStats teamAvgStats = teamStatsInfo.get(teamId).average();
                    if (teamAvgStats.ppg() > m_ppg)
                    {
                        m_ppg = teamAvgStats.ppg();
                    }
                    if (teamAvgStats.rpg() > m_rpg)
                    {
                        m_rpg = teamAvgStats.rpg();
                    }
                    if (teamAvgStats.apg() > m_apg)
                    {
                        m_apg = teamAvgStats.apg();
                    }
                    if (teamAvgStats.spg() > m_spg)
                    {
                        m_spg = teamAvgStats.spg();
                    }
                    if (teamAvgStats.bpg() > m_bpg)
                    {
                        m_bpg = teamAvgStats.bpg();
                    }
                    if (teamAvgStats.fgp() > m_fgp)
                    {
                        m_fgp = teamAvgStats.fgp();
                    }
                    if (teamAvgStats.tpp() > m_tpp)
                    {
                        m_tpp = teamAvgStats.tpp();
                    }
                    if (teamAvgStats.ftp() > m_ftp)
                    {
                        m_ftp = teamAvgStats.ftp();
                    }
                    if (teamAvgStats.orpg() > m_opg)
                    {
                        m_opg = teamAvgStats.orpg();
                    }
                    if (teamAvgStats.drpg() > m_dpg)
                    {
                        m_dpg = teamAvgStats.drpg();
                    }
                    if (teamAvgStats.topg() > m_tpg)
                    {
                        m_tpg = teamAvgStats.topg();
                    }
                    if (teamAvgStats.pfpg() > m_fpg)
                    {
                        m_fpg = teamAvgStats.pfpg();
                    }
                }
                Map<String, String> updateItem = new TreeMap<>();
                if (!statsDao.ppg().equals(m_ppg))
                {
                    updateItem.put("g_ppg", Utility.toString(m_ppg));
                }
                if (!statsDao.rpg().equals(m_rpg))
                {
                    updateItem.put("g_rpg", Utility.toString(m_rpg));
                }
                if (!statsDao.apg().equals(m_apg))
                {
                    updateItem.put("g_apg", Utility.toString(m_apg));
                }
                if (!statsDao.spg().equals(m_spg))
                {
                    updateItem.put("g_spg", Utility.toString(m_spg));
                }
                if (!statsDao.bpg().equals(m_bpg))
                {
                    updateItem.put("g_bpg", Utility.toString(m_bpg));
                }
                if (!statsDao.fgp().equals(m_fgp))
                {
                    updateItem.put("g_fgp", Utility.toString(m_fgp));
                }
                if (!statsDao.tpp().equals(m_tpp))
                {
                    updateItem.put("g_tpp", Utility.toString(m_tpp));
                }
                if (!statsDao.ftp().equals(m_ftp))
                {
                    updateItem.put("g_ftp", Utility.toString(m_ftp));
                }
                if (!statsDao.opg().equals(m_opg))
                {
                    updateItem.put("g_opg", Utility.toString(m_opg));
                }
                if (!statsDao.dpg().equals(m_dpg))
                {
                    updateItem.put("g_dpg", Utility.toString(m_dpg));
                }
                if (!statsDao.tpg().equals(m_tpg))
                {
                    updateItem.put("g_tpg", Utility.toString(m_tpg));
                }
                if (!statsDao.fpg().equals(m_fpg))
                {
                    updateItem.put("g_fpg", Utility.toString(m_fpg));
                }
                if (!Utility.isEmpty(updateItem))
                {
                    connect().stats().updateStats(updateItem, statsDao.season(), statsDao.seasonStage(), statsDao.statsType());
                }
            }
            if (statsDao.statsType().equals(StatsType.PLAYER))
            {
                Float m_ppg = 0f;
                Float m_rpg = 0f;
                Float m_apg = 0f;
                Float m_spg = 0f;
                Float m_bpg = 0f;
                List<BoxscoreDao> teamStatsInfo = connect().boxscore().selectPlayerStats(statsDao.season(), statsDao.seasonStage());
                for (BoxscoreDao dao : teamStatsInfo)
                {
                    AverageStats teamAvgStats = dao.average();
                    if (teamAvgStats.ppg() > m_ppg)
                    {
                        m_ppg = teamAvgStats.ppg();
                    }
                    if (teamAvgStats.rpg() > m_rpg)
                    {
                        m_rpg = teamAvgStats.rpg();
                    }
                    if (teamAvgStats.apg() > m_apg)
                    {
                        m_apg = teamAvgStats.apg();
                    }
                    if (teamAvgStats.spg() > m_spg)
                    {
                        m_spg = teamAvgStats.spg();
                    }
                    if (teamAvgStats.bpg() > m_bpg)
                    {
                        m_bpg = teamAvgStats.bpg();
                    }
                }
                Map<String, String> updateItem = new TreeMap<>();
                if (!statsDao.ppg().equals(m_ppg))
                {
                    updateItem.put("g_ppg", Utility.toString(m_ppg));
                }
                if (!statsDao.rpg().equals(m_rpg))
                {
                    updateItem.put("g_rpg", Utility.toString(m_rpg));
                }
                if (!statsDao.apg().equals(m_apg))
                {
                    updateItem.put("g_apg", Utility.toString(m_apg));
                }
                if (!statsDao.spg().equals(m_spg))
                {
                    updateItem.put("g_spg", Utility.toString(m_spg));
                }
                if (!statsDao.bpg().equals(m_bpg))
                {
                    updateItem.put("g_bpg", Utility.toString(m_bpg));
                }
                if (!Utility.isEmpty(updateItem))
                {
                    connect().stats().updateStats(updateItem, statsDao.season(), statsDao.seasonStage(), statsDao.statsType());
                }
            }
        }
    }

    private void playerSynch()
    {
        Map<Integer, PlayerDao> playerInfo = connect().player().selectPlayerInfo();
        Map<Integer, PlayerInfoPackage> playerInfoPackageMap = new HashMap<>();
        JsonObject jsonData = JsonAnalyse.getInstance("http://data.nba.net/10s/prod/v1/" + GAME_SEASON + "/players.json").getJsonObject().get("league").getAsJsonObject();
        for (PlayerLeague m_league : EnumSet.allOf(PlayerLeague.class))
        {
            Iterator<JsonElement> jsonPlayerInfoList = jsonData.get(m_league.leagueName()).getAsJsonArray().iterator();
            while (jsonPlayerInfoList.hasNext())
            {
                JsonObject jsonPlayerInfo = jsonPlayerInfoList.next().getAsJsonObject();
                Integer jsonPlayerId = Utility.toInteger(jsonPlayerInfo.get("personId").getAsString());
                if (!playerInfoPackageMap.containsKey(jsonPlayerId))
                {
                    playerInfoPackageMap.put(jsonPlayerId, new PlayerInfoPackage());
                }
                PlayerInfoPackage playerInfoPackage = playerInfoPackageMap.get(jsonPlayerId);
                playerInfoPackage.setInfo(m_league, jsonPlayerInfo);
                playerInfoPackageMap.put(jsonPlayerId, playerInfoPackage);
            }
        }
        Set<Integer> jsonPlayerIdSet = playerInfoPackageMap.keySet();
        for (Integer playerId : jsonPlayerIdSet)
        {
            PlayerInfoPackage packageData = playerInfoPackageMap.get(playerId);
            Map<String, String> data = new HashMap<>();
            data.put("p_position", Utility.toString(packageData.pos1()));
            data.put("p_position_2", Utility.toString(packageData.pos2()));
            data.put("p_height", Utility.toString(packageData.height()));
            data.put("p_weight", Utility.toString(packageData.weight()));
            data.put("p_birth_date", packageData.birthDate());
            data.put("p_country", packageData.country());
            data.put("t_id", Utility.toString(packageData.teamId()));
            data.put("p_jersey", Utility.toString(packageData.jersey()));
            data.put("p_standard_flg", packageData.isStandard() ? "1" : "0");
            data.put("p_africa_flg", packageData.isAfrica() ? "1" : "0");
            data.put("p_sacramento_flg", packageData.isSacramento() ? "1" : "0");
            data.put("p_vegas_flg", packageData.isVegas() ? "1" : "0");
            data.put("p_utah_flg", packageData.isUtah() ? "1" : "0");
            data.put("view_flg", packageData.isActive() ? "1" : "0");
            if (playerInfo.containsKey(packageData.playerId()))
            {
                connect().player().updatePlayer(data, playerId);
            }
            else
            {
                data.put("p_id", Utility.toString(packageData.playerId()));
                data.put("p_first_name", packageData.firstName());
                data.put("p_last_name", packageData.lastName());
                data.put("p_name", "");
                data.put("p_name_alphabet", "0");
                data.put("p_name_cnf_flg", "0");
                connect().player().insertPlayer(data);
            }
        }
        for (Integer playerId : playerInfo.keySet())
        {
            if (!jsonPlayerIdSet.contains(playerId))
            {
                Map<String, String> data = new HashMap<>();
                data.put("t_id", "0");
                data.put("p_jersey", "-1");
                data.put("p_standard_flg", "0");
                data.put("p_africa_flg", "0");
                data.put("p_sacramento_flg", "0");
                data.put("p_vegas_flg", "0");
                data.put("p_utah_flg", "0");
                data.put("view_flg", "0");
                connect().player().updatePlayer(data, playerId);
            }
        }
    }

    private void standingSynch()
    {
        Map<Integer, StandingsDao> standingsInfo = connect().standings().selectStandings();
        Iterator<JsonElement> teamStandings = JsonAnalyse.getInstance("http://data.nba.net/10s/prod/v1/current/standings_all.json").getJsonObject() //
                .get("league").getAsJsonObject() //
                .get("standard").getAsJsonObject() //
                .get("teams").getAsJsonArray().iterator();
        while (teamStandings.hasNext())
        {
            JsonObject teamStandingsInfo = teamStandings.next().getAsJsonObject();
            Integer teamId = Utility.toInteger(teamStandingsInfo.get("teamId").getAsString());
            StandingsDao teamDao = standingsInfo.get(teamId);
            Map<String, String> updateItem = new HashMap<>();
            if (!teamDao.win().equals(teamStandingsInfo.get("win").getAsInt()))
            {
                updateItem.put("t_win", teamStandingsInfo.get("win").getAsString());
            }
            if (!teamDao.loss().equals(teamStandingsInfo.get("loss").getAsInt()))
            {
                updateItem.put("t_loss", teamStandingsInfo.get("loss").getAsString());
            }
            if (!teamDao.winPercent().equals(teamStandingsInfo.get("winPctV2").getAsFloat()))
            {
                updateItem.put("t_win_percent", teamStandingsInfo.get("winPctV2").getAsString());
            }
            if (!teamDao.gameBehind().equals(teamStandingsInfo.get("gamesBehind").getAsFloat()))
            {
                updateItem.put("t_game_behind", teamStandingsInfo.get("gamesBehind").getAsString());
            }
            if (!teamDao.gameBehindDiv().equals(teamStandingsInfo.get("divGamesBehind").getAsFloat()))
            {
                updateItem.put("t_game_behind_div", teamStandingsInfo.get("divGamesBehind").getAsString());
            }
            Integer confRank = 0;
            if (teamStandingsInfo.get("confRank").getAsString().length() > 0)
            {
                confRank = teamStandingsInfo.get("confRank").getAsInt();
            }
            if (!teamDao.confRank().equals(confRank))
            {
                updateItem.put("t_conf_rank", Utility.toString(confRank));
            }
            if (!teamDao.confWin().equals(teamStandingsInfo.get("confWin").getAsInt()))
            {
                updateItem.put("t_conf_win", teamStandingsInfo.get("confWin").getAsString());
            }
            if (!teamDao.confLoss().equals(teamStandingsInfo.get("confLoss").getAsInt()))
            {
                updateItem.put("t_conf_loss", teamStandingsInfo.get("confLoss").getAsString());
            }
            Integer divRank = 0;
            if (teamStandingsInfo.get("divRank").getAsString().length() > 0)
            {
                divRank = teamStandingsInfo.get("divRank").getAsInt();
            }
            if (!teamDao.divRank().equals(divRank))
            {
                updateItem.put("t_div_rank", Utility.toString(divRank));
            }
            if (!teamDao.divWin().equals(teamStandingsInfo.get("divWin").getAsInt()))
            {
                updateItem.put("t_div_win", teamStandingsInfo.get("divWin").getAsString());
            }
            if (!teamDao.divLoss().equals(teamStandingsInfo.get("divLoss").getAsInt()))
            {
                updateItem.put("t_div_loss", teamStandingsInfo.get("divLoss").getAsString());
            }
            if (!teamDao.homeWin().equals(teamStandingsInfo.get("homeWin").getAsInt()))
            {
                updateItem.put("t_home_win", teamStandingsInfo.get("homeWin").getAsString());
            }
            if (!teamDao.homeLoss().equals(teamStandingsInfo.get("homeLoss").getAsInt()))
            {
                updateItem.put("t_home_loss", teamStandingsInfo.get("homeLoss").getAsString());
            }
            if (!teamDao.awayWin().equals(teamStandingsInfo.get("awayWin").getAsInt()))
            {
                updateItem.put("t_away_win", teamStandingsInfo.get("awayWin").getAsString());
            }
            if (!teamDao.awayLoss().equals(teamStandingsInfo.get("awayLoss").getAsInt()))
            {
                updateItem.put("t_away_loss", teamStandingsInfo.get("awayLoss").getAsString());
            }
            if (!teamDao.lastTenWin().equals(teamStandingsInfo.get("lastTenWin").getAsInt()))
            {
                updateItem.put("t_last_ten_win", teamStandingsInfo.get("lastTenWin").getAsString());
            }
            if (!teamDao.lastTenLoss().equals(teamStandingsInfo.get("lastTenLoss").getAsInt()))
            {
                updateItem.put("t_last_ten_loss", teamStandingsInfo.get("lastTenLoss").getAsString());
            }
            if (!teamDao.winStreak().equals(teamStandingsInfo.get("isWinStreak").getAsBoolean()))
            {
                updateItem.put("t_win_streak_flg", teamStandingsInfo.get("isWinStreak").getAsBoolean() ? "1" : "0");
            }
            if (!teamDao.streak().equals(teamStandingsInfo.get("streak").getAsInt()))
            {
                updateItem.put("t_streak", teamStandingsInfo.get("streak").getAsString());
            }
            if (!Utility.isEmpty(updateItem))
            {
                connect().standings().updateStandings(updateItem, teamId);
            }
        }
    }

    private List<Integer> getExceptionList()
    {
        List<Integer> result = new ArrayList<>();
        result.add(31700099);
        result.add(31800099);
        result.add(31900099);
        return result;
    }

    private String getStartTimeString(String param)
    {
        return Utility.getDate(Utility.getDateByOracleDateString(param), ConstDatetime.DATETIME);
    }

    private String getPositionNumber(String position)
    {
        if (position.toUpperCase().equals("C"))
        {
            return "1";
        }
        else if (position.toUpperCase().equals("PF"))
        {
            return "2";
        }
        else if (position.toUpperCase().equals("SF"))
        {
            return "3";
        }
        else if (position.toUpperCase().equals("SG"))
        {
            return "4";
        }
        else if (position.toUpperCase().equals("PG"))
        {
            return "5";
        }
        else
        {
            return "0";
        }
    }

    private String[] transMinuteSecond(Integer seconds)
    {
        Integer min = seconds / 60;
        Integer sec = seconds - min * 60;
        String result = min + "," + sec;
        return result.split(",");
    }

    private String getSort(Map<String, String> param)
    {
        Float result = (float) (Utility.toFloat(param.get("g_points")) + Utility.toFloat(param.get("g_rebounds")) + Utility.toFloat(param.get("g_assists"))
                + 1.4 * (Utility.toFloat(param.get("g_steals")) + Utility.toFloat(param.get("g_blocks"))) + 1.5 * Utility.toFloat(param.get("g_field_goals_made"))
                + 0.25 * Utility.toFloat(param.get("g_free_throw_made")) - 0.7 * Utility.toFloat(param.get("g_turnovers")) - 0.8 * (Utility.toFloat(param.get("g_field_goals_attempted"))
                        + Utility.toFloat(param.get("g_free_throw_attempted")) - Utility.toFloat(param.get("g_field_goals_made")) - Utility.toFloat(param.get("g_free_throw_made"))));
        return Utility.toString(result);
    }

    private String getDd2(Map<String, String> param)
    {
        Integer result = 0;
        if (Utility.toInteger(param.get("g_points")) > 9)
        {
            result++;
        }
        if (Utility.toInteger(param.get("g_rebounds")) > 9)
        {
            result++;
        }
        if (Utility.toInteger(param.get("g_assists")) > 9)
        {
            result++;
        }
        if (Utility.toInteger(param.get("g_steals")) > 9)
        {
            result++;
        }
        if (Utility.toInteger(param.get("g_blocks")) > 9)
        {
            result++;
        }
        if (result.equals(2))
        {
            return "1";
        }
        else
        {
            return "0";
        }
    }

    private String getTd3(Map<String, String> param)
    {
        Integer result = 0;
        if (Utility.toInteger(param.get("g_points")) > 9)
        {
            result++;
        }
        if (Utility.toInteger(param.get("g_rebounds")) > 9)
        {
            result++;
        }
        if (Utility.toInteger(param.get("g_assists")) > 9)
        {
            result++;
        }
        if (Utility.toInteger(param.get("g_steals")) > 9)
        {
            result++;
        }
        if (Utility.toInteger(param.get("g_blocks")) > 9)
        {
            result++;
        }
        if (result > 2)
        {
            return "1";
        }
        else
        {
            return "0";
        }
    }

    private String getEject(Map<String, String> param)
    {
        if (Utility.toInteger(param.get("g_personal_fouls")) > 5)
        {
            return "1";
        }
        else
        {
            return "0";
        }
    }

    public enum TeamArena implements Parameters
    {
        ATL("1610612737", "State Farm Arena"),

        BOS("1610612738", "TD Garden"),

        CLE("1610612739", "Rocket Mortgage FieldHouse"),

        NOP("1610612740", "Smoothie King Center"),

        CHI("1610612741", "United Center"),

        DAL("1610612742", "American Airlines Center"),

        DEN("1610612743", "Pepsi Center"),

        GSW("1610612744", "Chase Center"),

        HOU("1610612745", "Toyota Center"),

        LAC("1610612746", "Staples Center"),

        LAL("1610612747", "Staples Center"),

        MIA("1610612748", "AmericanAirlines Arena"),

        MIL("1610612749", "Fiserv Forum"),

        MIN("1610612750", "Target Center"),

        BKN("1610612751", "Barclays Center"),

        NYK("1610612752", "Madison Square Garden"),

        ORL("1610612753", "Amway Center"),

        IND("1610612754", "Bankers Life Fieldhouse"),

        PHI("1610612755", "Wells Fargo Center"),

        PHX("1610612756", "Talking Stick Resort Arena"),

        POR("1610612757", "Moda Center"),

        SAC("1610612758", "Golden 1 Center"),

        SAS("1610612759", "AT&T Center"),

        OKC("1610612760", "Chesapeake Energy Arena"),

        TOR("1610612761", "Scotiabank Arena"),

        UTA("1610612762", "Vivint Smart Home Arena"),

        MEM("1610612763", "FedExForum"),

        WAS("1610612764", "Capital One Arena"),

        DET("1610612765", "Little Caesars Arena"),

        CHA("1610612766", "Spectrum Center"),

        UNKNOWN("", "");

        private String m_val;
        private String m_arenaName;

        private TeamArena(String val, String arenaName)
        {
            m_val = val;
            m_arenaName = arenaName;
        }

        @Override
        public String val()
        {
            return m_val;
        }

        public String arenaName()
        {
            return m_arenaName;
        }

        @Override
        public Parameters unknown()
        {
            return UNKNOWN;
        }
    }
}
