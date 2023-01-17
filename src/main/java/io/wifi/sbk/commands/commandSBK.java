package io.wifi.sbk.commands;

import com.mojang.brigadier.CommandDispatcher;

// import static net.minecraft.command.argument.ScoreHolderArgumentType;

import static net.minecraft.server.command.CommandManager.literal;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import io.wifi.sbk.sbk;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import static net.minecraft.server.command.CommandManager.argument;

// import net.minecraft.text.TranslatableText;
public class commandSBK {

    // private static final SimpleCommandExceptionType EDIT_TAG_EXCEPTION = new
    // SimpleCommandExceptionType(new
    // TranslatableText("arguments.editfunction.tag.unsupported"));
    // private static final SimpleCommandExceptionType MOD_NOT_INSTALLED_EXCEPTION =
    // new SimpleCommandExceptionType(new
    // TranslatableText("commands.editfunction.failed.modNotInstalled"));
    public static final SuggestionProvider<ServerCommandSource> getOperatingSuggestion = (ctx, builder) -> {
        for (int i = 0; i < sbk.backupFiles.size(); i++) {
            builder.suggest(sbk.backupFiles.get(i));
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("sbk")
                .requires(source -> source.hasPermissionLevel(3))
                .executes(command -> {
                    command.getSource().sendFeedback(Text.literal("※ Help Infomation: /sbk help"), false);
                    return 1;
                })
                .then(literal("reload").executes(command -> {
                    sbk.reloadConfig();
                    command.getSource().sendFeedback(Text.literal("√ Reload the SBK config successfully."), true);
                    return 1;
                }))
                .then(literal("help").executes(command -> {
                    command.getSource().sendFeedback(Text.literal(
                            "/sbk reload - Reload\n/sbk backup - Backup\n/sbk restore - Restore\n/backup list - List the scoreboards that will be backuped and the backup files."),
                            false);
                    return 1;
                }))
                .then(literal("backup").executes(command -> {
                    command.getSource().sendFeedback(Text.literal("Backing up the scores..."),
                            true);
                    if (sbk.backupScores(command.getSource().getServer(), "backup")) {
                        command.getSource().sendFeedback(Text.literal("Backup the scores successfully!"),
                                true);
                    } else {
                        command.getSource().sendFeedback(Text.literal("Failed to backup the scores!"),
                                true);
                    }
                    ;

                    return 1;
                }).then(argument("path", StringArgumentType.greedyString()).suggests(getOperatingSuggestion)
                        .executes(command -> {
                            String path = StringArgumentType.getString(command,
                                    "path");
                            command.getSource().sendFeedback(
                                    Text.literal("Backing up the scores to '" + path + "'"),
                                    true);

                            if (sbk.backupScores(command.getSource().getServer(), path)) {
                                command.getSource().sendFeedback(
                                        Text.literal("Backup the scores to '" + path + "' successfully!"),
                                        true);
                            } else {
                                command.getSource().sendFeedback(
                                        Text.literal("Failed to backup the scores to '" + path + "'!"),
                                        true);
                            }

                            return 1;
                        })))
                .then(literal("restore").executes(command -> {
                    command.getSource().sendFeedback(Text.literal("Restoring the scores..."),
                            true);
                    if (sbk.restoreScores(command.getSource().getServer(), "backup"))
                        command.getSource().sendFeedback(Text.literal("Restore the scores successfully!"),
                                true);
                    else
                        command.getSource().sendFeedback(Text.literal("Restore the scores failed!"),
                                true);
                    return 1;
                }).then(argument("path", StringArgumentType.greedyString()).suggests(getOperatingSuggestion)
                        .executes(command -> {
                            String path = StringArgumentType.getString(command,
                                    "path");
                            command.getSource().sendFeedback(
                                    Text.literal("Restoring the scores from '" + path + "'..."),
                                    true);
                            if (sbk.restoreScores(command.getSource().getServer(), path)) {
                                command.getSource().sendFeedback(
                                        Text.literal("Restore the scores from '" + path + "' successfully!"),
                                        true);
                            } else {
                                command.getSource().sendFeedback(
                                        Text.literal("Failed to restore the scores from '" + path + "'!"),
                                        true);
                            }
                            ;

                            return 1;
                        })))
                .then(literal("list").executes(command -> {
                    String temp = "";
                    for (int i = 0; i < sbk.BackupScoreboards.length; i++) {
                        temp += (temp == "" ? "" : ", ") + sbk.BackupScoreboards[i];
                    }
                    String tempp = "";
                    for (int i = 0; i < sbk.backupFiles.size(); i++) {
                        tempp += "\n\u00a7d[\u00a76" + (i + 1) + "] \u00a7e" + (sbk.backupFiles.get(i));
                    }
                    command.getSource().sendFeedback(Text.literal(
                            "Backup Scoreboards: [" + temp + "]\n\u00a7a---------- Backup Files ----------" + tempp
                                    + "\n\u00a7b (Use \u00a76/backup reload\u00a7b to reflush the file list)"),
                            false);
                    return 1;
                }))

        );
    }
}
