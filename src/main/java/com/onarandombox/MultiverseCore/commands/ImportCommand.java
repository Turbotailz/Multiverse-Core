/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2020.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.commandTools.WorldFlags;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("mv")
public class ImportCommand extends MultiverseCommand {

    public ImportCommand(MultiverseCore plugin) {
        super(plugin);
    }

    @Subcommand("import")
    @CommandPermission("multiverse.core.import")
    @Syntax("<name> <env> -g [generator[:id]] [-n]")
    @CommandCompletion("@potentialWorlds @environments -g|-n")
    @Description("Imports a new world into multiverse.")
    public void onImportCommand(@NotNull CommandSender sender,

                                @Syntax("<name>")
                                @Description("Folder name of the world.")
                                @NotNull @Flags("trim") @Conditions("importableWorldName") String worldName,

                                @Syntax("<env>")
                                @Description("The world's environment. See: /mv env")
                                @NotNull World.Environment environment,

                                @Syntax("-g [generator[:id]] [-n]")
                                @Description("Other world settings. See: http://gg.gg/nn8c2")
                                @Nullable @Optional String[] flagsArray) {

        WorldFlags flags = new WorldFlags(sender, this.plugin, flagsArray);

        Command.broadcastCommandMessage(sender, "Starting import of world '" + worldName + "'...");
        String resultMessage = (this.plugin.getMVWorldManager().addWorld(worldName,
                environment,
                null,
                null,
                null,
                flags.getGenerator(),
                flags.isSpawnAdjust())
        )
                ? ChatColor.GREEN + "Complete!"
                : ChatColor.RED + "Failed! See console for more details.";

        Command.broadcastCommandMessage(sender, resultMessage);
    }
}
