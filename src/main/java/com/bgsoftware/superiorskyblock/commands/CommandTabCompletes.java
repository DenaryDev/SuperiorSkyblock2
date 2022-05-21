package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.impl.internal.SuperiorMenuCustom;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class CommandTabCompletes {

    private CommandTabCompletes() {

    }

    public static List<String> getPlayerIslandsExceptSender(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument, boolean hideVanish) {
        return getPlayerIslandsExceptSender(plugin, sender, argument, hideVanish, (onlinePlayer, onlineIsland) -> true);
    }

    public static List<String> getPlayerIslandsExceptSender(SuperiorSkyblockPlugin plugin, CommandSender sender,
                                                            String argument, boolean hideVanish,
                                                            BiPredicate<SuperiorPlayer, Island> islandPredicate) {
        SuperiorPlayer superiorPlayer = sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null;
        Island island = superiorPlayer == null ? null : superiorPlayer.getIsland();
        return getOnlinePlayersWithIslands(plugin, argument, hideVanish, (onlinePlayer, onlineIsland) ->
                onlineIsland != null && (superiorPlayer == null || island == null || !island.equals(onlineIsland)) &&
                        islandPredicate.test(onlinePlayer, onlineIsland));
    }

    public static List<String> getIslandMembersWithLowerRole(Island island, String argument, PlayerRole maxRole) {
        return getIslandMembers(island, argument, islandMember -> islandMember.getPlayerRole().isLessThan(maxRole));
    }

    public static List<String> getIslandMembers(Island island, String argument, Predicate<SuperiorPlayer> predicate) {
        return getPlayers(island.getIslandMembers(false), argument, predicate);
    }

    public static List<String> getIslandMembers(Island island, String argument) {
        return getPlayers(island.getIslandMembers(false), argument);
    }

    public static List<String> getOnlinePlayers(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Bukkit.getOnlinePlayers().stream().map(plugin.getPlayers()::getSuperiorPlayer)
                .filter(onlinePlayer -> (!hideVanish || onlinePlayer.isShownAsOnline()) &&
                        onlinePlayer.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .map(SuperiorPlayer::getName).collect(Collectors.toList());
    }

    public static List<String> getOnlinePlayers(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish, Predicate<SuperiorPlayer> predicate) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Bukkit.getOnlinePlayers().stream().map(plugin.getPlayers()::getSuperiorPlayer)
                .filter(onlinePlayer -> (!hideVanish || onlinePlayer.isShownAsOnline()) &&
                        predicate.test(onlinePlayer) && onlinePlayer.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .map(SuperiorPlayer::getName).collect(Collectors.toList());
    }

    public static List<String> getOnlinePlayersWithIslands(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish) {
        Set<String> tabArguments = new HashSet<>();
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);

        for (Player player : Bukkit.getOnlinePlayers()) {
            SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
            if (!hideVanish || onlinePlayer.isShownAsOnline()) {
                Island onlineIsland = onlinePlayer.getIsland();
                if (onlinePlayer.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                    tabArguments.add(onlinePlayer.getName());
                if (onlineIsland != null && onlineIsland.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                    tabArguments.add(onlineIsland.getName());
            }
        }

        return new ArrayList<>(tabArguments);
    }

    public static List<String> getOnlinePlayersWithIslands(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish, BiPredicate<SuperiorPlayer, Island> predicate) {
        Set<String> tabArguments = new HashSet<>();
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);

        for (Player player : Bukkit.getOnlinePlayers()) {
            SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
            if (!hideVanish || onlinePlayer.isShownAsOnline()) {
                Island onlineIsland = onlinePlayer.getIsland();
                if (predicate.test(onlinePlayer, onlineIsland)) {
                    if (onlinePlayer.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                        tabArguments.add(onlinePlayer.getName());
                    if (onlineIsland != null && onlineIsland.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                        tabArguments.add(onlineIsland.getName());
                }
            }
        }

        return new ArrayList<>(tabArguments);
    }

    public static List<String> getIslandWarps(Island island, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return island.getIslandWarps().keySet().stream()
                .filter(warpName -> warpName.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getIslandVisitors(Island island, String argument) {
        return getPlayers(island.getIslandVisitors(), argument);
    }

    public static List<String> getCustomComplete(String argument, String... tabVariables) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Stream.of(tabVariables).filter(var -> var.contains(lowerArgument)).collect(Collectors.toList());
    }

    public static List<String> getCustomComplete(String argument, Predicate<String> predicate, String... tabVariables) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Stream.of(tabVariables).filter(var -> var.contains(lowerArgument) && predicate.test(var)).collect(Collectors.toList());
    }

    public static List<String> getCustomComplete(String argument, IntStream tabVariables) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Stream.of(tabVariables).map(i -> i + "").filter(var -> var.contains(lowerArgument)).collect(Collectors.toList());
    }

    public static List<String> getSchematics(SuperiorSkyblockPlugin plugin, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return plugin.getSchematics().getSchematics().stream().filter(schematic -> !schematic.endsWith("_nether") &&
                !schematic.endsWith("_the_end") && schematic.toLowerCase(Locale.ENGLISH).contains(lowerArgument)).collect(Collectors.toList());
    }

    public static List<String> getIslandBannedPlayers(Island island, String argument) {
        return getPlayers(island.getBannedPlayers(), argument);
    }

    public static List<String> getUpgrades(SuperiorSkyblockPlugin plugin, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return plugin.getUpgrades().getUpgrades().stream()
                .map(Upgrade::getName)
                .filter(name -> name.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getPlayerRoles(SuperiorSkyblockPlugin plugin, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return plugin.getRoles().getRoles().stream()
                .map(PlayerRole::toString)
                .filter(playerRoleName -> playerRoleName.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getPlayerRoles(SuperiorSkyblockPlugin plugin, String argument, Predicate<PlayerRole> predicate) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return plugin.getRoles().getRoles().stream()
                .filter(playerRole -> predicate.test(playerRole) && playerRole.toString().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .map(PlayerRole::toString).collect(Collectors.toList());
    }

    public static List<String> getMaterials(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Materials.getBlocksNonLegacy().stream()
                .filter(material -> material.isBlock() && !Materials.isLegacy(material))
                .map(material -> material.name().toLowerCase(Locale.ENGLISH))
                .filter(materialName -> materialName.contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getPotionEffects(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Stream.of(PotionEffectType.values()).filter(potionEffectType -> {
            try {
                return potionEffectType != null && potionEffectType.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument);
            } catch (Exception ex) {
                return false;
            }
        }).map(PotionEffectType::getName).collect(Collectors.toList());
    }

    public static List<String> getEntitiesForLimit(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Stream.of(EntityType.values())
                .filter(EntityUtils::canHaveLimit)
                .map(entityType -> entityType.name().toLowerCase(Locale.ENGLISH))
                .filter(entityTypeName -> entityTypeName.contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getMaterialsForGenerators(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Materials.getSolids().stream()
                .map(material -> material.name().toLowerCase(Locale.ENGLISH))
                .filter(materialName -> materialName.contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getAllMissions(SuperiorSkyblockPlugin plugin) {
        return plugin.getMissions().getAllMissions().stream().map(Mission::getName).collect(Collectors.toList());
    }

    public static List<String> getMissions(SuperiorSkyblockPlugin plugin, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return plugin.getMissions().getAllMissions().stream()
                .map(Mission::getName)
                .filter(name -> name.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getMenus(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return SuperiorMenuCustom.getCustomMenus().stream()
                .filter(menu -> menu.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getBiomes(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Stream.of(Biome.values())
                .map(biome -> biome.name().toLowerCase(Locale.ENGLISH))
                .filter(biomeName -> biomeName.contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getWorlds(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> name.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getIslandPrivileges(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return IslandPrivilege.values().stream()
                .map(islandPrivilege -> islandPrivilege.getName().toLowerCase(Locale.ENGLISH))
                .filter(islandPrivilegeName -> islandPrivilegeName.contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getRatedPlayers(SuperiorSkyblockPlugin plugin, Island island, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return island.getRatings().keySet().stream()
                .map(playerUUID -> plugin.getPlayers().getSuperiorPlayer(playerUUID).getName())
                .filter(name -> name.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getRatings(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return IslandPrivilege.values().stream()
                .map(IslandPrivilege::getName)
                .filter(name -> name.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getIslandFlags(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return IslandFlag.values().stream()
                .map(islandFlag -> islandFlag.getName().toLowerCase(Locale.ENGLISH))
                .filter(islandFlagName -> islandFlagName.contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getEnvironments(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Arrays.stream(World.Environment.values())
                .map(environment -> environment.name().toLowerCase(Locale.ENGLISH))
                .filter(environmentName -> environmentName.contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getBorderColors(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return Stream.of(BorderColor.values())
                .map(borderColor -> borderColor.name().toLowerCase(Locale.ENGLISH))
                .filter(borderColorName -> borderColorName.contains(lowerArgument))
                .collect(Collectors.toList());
    }

    private static List<String> getPlayers(Collection<SuperiorPlayer> players, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return players.stream()
                .map(SuperiorPlayer::getName)
                .filter(name -> name.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .collect(Collectors.toList());
    }

    private static List<String> getPlayers(Collection<SuperiorPlayer> players, String argument, Predicate<SuperiorPlayer> predicate) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return players.stream().filter(player -> predicate.test(player) && player.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .map(SuperiorPlayer::getName).collect(Collectors.toList());
    }

}
