package com.onarandombox.MultiverseCore.commands_acf;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteFailedException;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteService;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceFactory;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceType;
import com.onarandombox.MultiverseCore.utils.webpaste.URLShortener;
import com.onarandombox.MultiverseCore.utils.webpaste.URLShortenerFactory;
import com.onarandombox.MultiverseCore.utils.webpaste.URLShortenerType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

@CommandAlias("mv")
public class VersionCommand extends MultiverseCommand {

    private static final URLShortener SHORTENER = URLShortenerFactory.getService(URLShortenerType.BITLY);

    public VersionCommand(MultiverseCore plugin) {
        super(plugin);
    }

    //TODO: Think, Is there a point in keeping the old flags?
    @Subcommand("version")
    @CommandPermission("multiverse.core.version")
    @Syntax("[pastebin|hastebin|pastegg] [--include-plugin-list|-pl]")
    @CommandCompletion("@pasteTypes --include-plugin-list|-pl")
    @Description("Dumps version info to the console, optionally to pastal service.")
    public void onVersionCommand(@NotNull CommandSender sender,
                                 @NotNull PasteServiceType pasteType,
                                 @Nullable @Optional @Single String includePlugin) {

        if (sender instanceof Player) {
            sender.sendMessage("Version info dumped to console! Please check your server logs.");
        }

        MVVersionEvent versionEvent = new MVVersionEvent();
        this.addVersionInfoToEvent(versionEvent);
        this.plugin.getServer().getPluginManager().callEvent(versionEvent);

        if (includePlugin != null && (includePlugin.equalsIgnoreCase("--include-plugin-list") || includePlugin.equalsIgnoreCase("-pl"))) {
            versionEvent.appendVersionInfo('\n' + "Plugins: " + getPluginList());
            versionEvent.putDetailedVersionInfo("plugins.txt", "Plugins: " + getPluginList());
        }

        String versionInfo = versionEvent.getVersionInfo();
        versionEvent.putDetailedVersionInfo("version.txt", versionInfo);

        Map<String, String> files = versionEvent.getDetailedVersionInfo();

        logToConsole(versionInfo);

        if (pasteType == PasteServiceType.NONE) {
            return;
        }

        BukkitRunnable logPoster = new BukkitRunnable() {
            @Override
            public void run() {
                String pasteUrl = postToService(pasteType, true, versionInfo, files);
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage("Version info dumped here: " + ChatColor.GREEN + pasteUrl);
                }
                Logging.info("Version info dumped here: %s", pasteUrl);
            }
        };

        // Run the log posting operation asynchronously, since we don't know how long it will take.
        logPoster.runTaskAsynchronously(this.plugin);
    }

    private void logToConsole(String versionInfo) {
        Arrays.stream(versionInfo.split("\\r?\\n"))
                .filter(line -> !line.isEmpty())
                .forEach(this.plugin.getServer().getLogger()::info);
    }

    private void addVersionInfoToEvent(MVVersionEvent event) {
        // add the legacy version info
        event.appendVersionInfo(this.getLegacyString());

        // add the legacy file, but as markdown so it's readable
        // TODO Readd this in 5.0.0
        // event.putDetailedVersionInfo("version.md", this.getMarkdownString());

        // add config.yml
        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        event.putDetailedVersionInfo("multiverse-core/config.yml", configFile);

        // add worlds.yml
        File worldsFile = new File(this.plugin.getDataFolder(), "worlds.yml");
        event.putDetailedVersionInfo("multiverse-core/worlds.yml", worldsFile);
    }

    /**
     * Send the current contents of this.pasteBinBuffer to a web service.
     *
     * @param type       Service type to send paste data to.
     * @param isPrivate  Should the paste be marked as private.
     * @param pasteData  Legacy string only data to post to a service.
     * @param pasteFiles Map of filenames/contents of debug info.
     * @return URL of visible paste
     */
    private static String postToService(PasteServiceType type, boolean isPrivate, String pasteData, Map<String, String> pasteFiles) {
        PasteService ps = PasteServiceFactory.getService(type, isPrivate);

        try {
            String result = (ps.supportsMultiFile())
                    ? ps.postData(pasteFiles)
                    : ps.postData(pasteData);

            return (SHORTENER != null) ? SHORTENER.shorten(result) : result;
        }
        catch (PasteFailedException e) {
            e.printStackTrace();
            return "Error posting to service.";
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return "That service isn't supported yet.";
        }
    }

    private String getLegacyString() {
        return "[Multiverse-Core] Multiverse-Core Version: " + this.plugin.getDescription().getVersion() + '\n'
                + "[Multiverse-Core] Bukkit Version: " + this.plugin.getServer().getVersion() + '\n'
                + "[Multiverse-Core] Loaded Worlds: " + this.plugin.getMVWorldManager().getMVWorlds() + '\n'
                + "[Multiverse-Core] Multiverse Plugins Loaded: " + this.plugin.getPluginCount() + '\n'
                + "[Multiverse-Core] Economy being used: " + plugin.getEconomist().getEconomyName() + '\n'
                + "[Multiverse-Core] Permissions Plugin: " + this.plugin.getMVPerms().getType() + '\n'
                + "[Multiverse-Core] Dumping Config Values: (version " + this.plugin.getMVConfig().getVersion() + ")" + '\n'
                + "[Multiverse-Core]   enforceaccess: " + plugin.getMVConfig().getEnforceAccess() + '\n'
                + "[Multiverse-Core]   prefixchat: " + plugin.getMVConfig().getPrefixChat() + '\n'
                + "[Multiverse-Core]   prefixchatformat: " + plugin.getMVConfig().getPrefixChatFormat() + '\n'
                + "[Multiverse-Core]   useasyncchat: " + plugin.getMVConfig().getUseAsyncChat() + '\n'
                + "[Multiverse-Core]   teleportintercept: " + plugin.getMVConfig().getTeleportIntercept() + '\n'
                + "[Multiverse-Core]   firstspawnoverride: " + plugin.getMVConfig().getFirstSpawnOverride() + '\n'
                + "[Multiverse-Core]   displaypermerrors: " + plugin.getMVConfig().getDisplayPermErrors() + '\n'
                + "[Multiverse-Core]   globaldebug: " + plugin.getMVConfig().getGlobalDebug() + '\n'
                + "[Multiverse-Core]   silentstart: " + plugin.getMVConfig().getSilentStart() + '\n'
                + "[Multiverse-Core]   messagecooldown: " + plugin.getMessaging().getCooldown() + '\n'
                + "[Multiverse-Core]   version: " + plugin.getMVConfig().getVersion() + '\n'
                + "[Multiverse-Core]   firstspawnworld: " + plugin.getMVConfig().getFirstSpawnWorld() + '\n'
                + "[Multiverse-Core]   teleportcooldown: " + plugin.getMVConfig().getTeleportCooldown() + '\n'
                + "[Multiverse-Core]   defaultportalsearch: " + plugin.getMVConfig().isUsingDefaultPortalSearch() + '\n'
                + "[Multiverse-Core]   portalsearchradius: " + plugin.getMVConfig().getPortalSearchRadius() + '\n'
                + "[Multiverse-Core]   autopurge: " + plugin.getMVConfig().isAutoPurgeEnabled() + '\n'
                //TODO: Still dont know what this special code means.
                + "[Multiverse-Core] Special Code: FRN002" + '\n';
    }

    private String getPluginList() {
        return StringUtils.join(plugin.getServer().getPluginManager().getPlugins(), ", ");
    }
}