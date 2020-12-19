package com.onarandombox.MultiverseCore.commands_acf;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("mv")
public class PurgeCommand extends MultiverseCommand {

    public PurgeCommand(MultiverseCore plugin) {
        super(plugin);
    }

    @Subcommand("purgeall")
    @CommandPermission("multiverse.core.purge")
    @Syntax("<all|animals|monsters|mobname>")
    @CommandCompletion("all|animals|monsters|@livingEntities")
    @Description("Removed the specified type of mob from all worlds.")
    public void onPurgeAllCommand(@NotNull CommandSender sender,
                                  @NotNull @Split(",") String[] targetEntities) {

        doPurge(sender, this.plugin.getMVWorldManager().getMVWorlds(), targetEntities);
    }

    @Subcommand("purge")
    @CommandPermission("multiverse.core.purge")
    @Syntax("<all|animals|monsters|mobname>")
    @CommandCompletion("@MVWorlds|all|animals|monsters|@livingEntities all|animals|monsters|@livingEntities")
    @Description("Removed the specified type of mob from the specified world.")
    public void onPurgeCommand(@NotNull CommandSender sender,
                               @NotNull @Flags("other,defaultself,fallbackself") MultiverseWorld world,
                               @NotNull @Split(",") String[] targetEntities) {

        doPurge(sender, Collections.singleton(world), targetEntities);
    }

    private void doPurge(@NotNull CommandSender sender,
                         @NotNull Collection<MultiverseWorld> targetWorlds,
                         @NotNull String[] targetEntities) {

        List<String> thingsToKill = Arrays.stream(targetEntities)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        targetWorlds.forEach(w -> this.plugin.getMVWorldManager()
                .getTheWorldPurger()
                .purgeWorld(w, thingsToKill, false, false, sender));
    }
}