package io.wifi.sbk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.wifi.sbk.commands.*;

public final class sbk implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors

    public static MyConfig MyConfig = null;
    public static String[] BackupScoreboards = null;
    public static Gson gson = new Gson();
    public static ArrayList<String> backupFiles = new ArrayList<String>();

    public static void reloadFileLists() {
        backupFiles.clear();
        File file = new File("./scorebackups");
        File[] listFiles = file.listFiles();// 获取当前路径下的所有文件和目录,返回File对象数组
        for (File f : listFiles) {// 将目录内的内容对象化并遍历
            if (f.isDirectory()) {// 如果是目录
                // 跳过
            } else if (f.isFile()) {// 如果是文件
                String name = f.getName();
                backupFiles.add(name.substring(0, name.lastIndexOf(".")));
            }
        }

    }

    public static void reloadConfig() {
        if (MyConfig == null) {
            MyConfig = new MyConfig("./config/sbk.properties");
        } else {
            MyConfig.reloadConfig("./config/sbk.properties");
        }
        String temp = MyConfig.getValue("scores", null);
        reloadFileLists();
        if (temp != null) {
            BackupScoreboards = temp.split("\\|");
        } else {
            // BackupScores={};
        }

    }

    public static final Logger LOGGER = LoggerFactory.getLogger("wifi_sbk");

    public static void regCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            commandSBK.register(dispatcher); // 注册
        });
    }

    public static boolean backupScores(MinecraftServer source, String path) {
        // System.out.println("Backup Score #1");
        ArrayList<eachscore> Scores = new ArrayList<eachscore>();
        // System.out.println("Backup Score #2");
        for (int i = 0; i < sbk.BackupScoreboards.length; i++) {
            // System.out.println("Backup Score #3");
            ScoreboardObjective scoobj = source.getScoreboard().getObjective(sbk.BackupScoreboards[i]);
            if (scoobj == null)
                continue;
            // System.out.println("Backup Score #4");
            Collection<ScoreboardPlayerScore> List = source.getScoreboard().getAllPlayerScores(scoobj);
            Iterator<ScoreboardPlayerScore> it = List.iterator();
            while (it.hasNext()) {
                ScoreboardPlayerScore obj = it.next();
                Scores.add(new eachscore(sbk.BackupScoreboards[i], obj.getPlayerName(), obj.getScore()));
            }
            // System.out.println("Backup Score #5");

        }
        // System.out.println("Backup Score #6");
        if (Scores.size() <= 0)
            return false;
        io.wifi.sbk.MyConfig.saveJSONobj(Scores, "./scorebackups/" + path + ".json");
        System.out.println("Wrote './scorebackups/" + path + ".json'");
        reloadFileLists();
        return true;

    }

    public static boolean restoreScores(MinecraftServer source, String Path) {
        ArrayList<eachscore> Scores = new ArrayList<eachscore>();
        String Content = MyConfig.getFileContent("./scorebackups/" + Path + ".json");
        try {
            Scores = gson.fromJson(Content, new TypeToken<ArrayList<eachscore>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            System.out.println("Failed to restore from './scorebackups/" + Path + ".json'");
            return false;
        }
        for (int i = 0; i < Scores.size(); i++) {
            String mcname = Scores.get(i).name;
            int mcvalue = Scores.get(i).value;
            ScoreboardObjective mcobjective = source.getScoreboard().getObjective(Scores.get(i).objective);
            // net.minecraft.server.command.ScoreboardCommand
            ScoreboardPlayerScore sps = source.getScoreboard().getPlayerScore(mcname, mcobjective);
            sps.setScore(mcvalue);
        }
        System.out.println("Restore from './scorebackups/" + Path + ".json'");
        return true;
    }

    public static void regEvents() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            Date date = new Date(System.currentTimeMillis());
            backupScores(server, "shutdown-" + formatter.format(date));
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        });
    }

    @Override
    public void onInitialize() {
        reloadConfig();
        regCommands();
        regEvents();
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Loaded Scoreboard Backup Successfully.");
    }
}
