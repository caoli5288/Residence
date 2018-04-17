package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.utils.BiFunctionRegistry;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlaceholderResPlayer extends EZPlaceholderHook {

    private final BiFunctionRegistry<Player, ?, String> registry = new BiFunctionRegistry<>();

    public PlaceholderResPlayer(Plugin plugin) {
        super(plugin, "resplayer");
        registry.register("maxres", (p, nil) -> {
            ResidencePlayer rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(p);
            return "" + rplayer.getMaxRes();
        });
        registry.register("maxx", (p, nil) -> {
            ResidencePlayer rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(p);
            PermissionGroup group = rplayer.getGroup();
            ResidencePlayerBean b = L2Pool.getResPlayer(p);
            return "" + group.getXmin() + "-" + (group.getXmax() + b.getMaxWeight());
        });
        registry.register("maxy", (p, nil) -> {
            ResidencePlayer rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(p);
            PermissionGroup group = rplayer.getGroup();
            ResidencePlayerBean b = L2Pool.getResPlayer(p);
            return "" + group.getYmin() + "-" + (group.getYmax() + b.getMaxHeight());
        });
        registry.register("maxz", (p, nil) -> {
            ResidencePlayer rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(p);
            PermissionGroup group = rplayer.getGroup();
            ResidencePlayerBean b = L2Pool.getResPlayer(p);
            return "" + group.getZmin() + "-" + (group.getZmax() + b.getMaxWeight());
        });
        registry.register("maxsub", (p, nil) -> {
            ResidencePlayer rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(p);
            return "" + rplayer.getMaxSubzones();
        });
        registry.register("maxsubdepth", (p, nil) -> {
            ResidencePlayer rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(p);
            return "" + rplayer.getMaxSubzoneDepth();
        });
        registry.register("cansetmessage", (p, nil) -> {
            ResidencePlayer rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(p);
            return "" + rplayer.getGroup().canSetEnterLeaveMessages();
        });
        registry.register("buyprice", (p, nil) -> {
            if (Residence.getInstance().getEconomyManager() == null) {
                return "-1";
            }
            ResidencePlayer rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(p);
            return "" + rplayer.getGroup().getCostperarea();
        });
    }

    @Override
    public String onPlaceholderRequest(Player player, String input) {
        return registry.handle(input, player, null);
    }
}
