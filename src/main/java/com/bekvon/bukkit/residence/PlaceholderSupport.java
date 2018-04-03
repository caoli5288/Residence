package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.bekvon.bukkit.residence.utils.BiFunctionRegistry;
import com.bekvon.bukkit.residence.utils.Pair;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static com.bekvon.bukkit.residence.$.nil;

public class PlaceholderSupport extends EZPlaceholderHook {

    private static PlaceholderSupport instance;

    private final BiFunctionRegistry<Player, Pair<ClaimedResidence, Iterator<String>>, String> registry = new BiFunctionRegistry<>();
    private final Residence plugin;

    public PlaceholderSupport(Residence plugin) {
        super(plugin, "residence");
        this.plugin = plugin;
        registry.register("owner", (p, pair) -> "" + pair.getKey().getOwner().equals(p.getName()));
        registry.register("flag", (p, pair) -> {
            ResidencePermissions permissions = pair.getKey().getPermissions();
            Iterator<String> itr = pair.getValue();
            Flags flag = Flags.valueOf(itr.next());
            if (itr.hasNext()) {
                Player nextp = Bukkit.getPlayerExact(itr.next());
                return String.valueOf(getFlagActually(permissions, nextp, flag));
            }
            return String.valueOf(getFlagActually(permissions, flag));
        });
    }

    public String onPlaceholderRequest(Player p, String input) {
        Iterator<String> itr = Arrays.asList(input.split(";")).iterator();
        String label = itr.next();
        if (label.equalsIgnoreCase("*")) {
            List<ClaimedResidence> all = plugin.getPlayerManager().getResidencePlayer(p).getResList();
            if (all.isEmpty()) {
                return "";
            }
            StringJoiner joiner = new StringJoiner(",");
            all.forEach(el -> joiner.add(el.getName()));
            return joiner.toString();
        }
        ClaimedResidence residence = plugin.getResidenceManager().getByName(label);
        if (nil(residence)) {
            return "null";
        }
        return String.valueOf(registry.handle(itr.next(), p, new Pair<>(residence, itr)));
    }

    protected boolean getFlagActually(ResidencePermissions permissions, Flags flag) {
        Map<String, Boolean> all = plugin.getPermissionManager().getAllFlags().getFlags();
        if (nil(permissions)) {
            return all.get(flag.getName());
        }
        return permissions.has(flag, all.get(flag.getName()));
    }

    protected boolean getFlagActually(ResidencePermissions permissions, Player p, Flags flag) {
        return permissions.playerHas(p, flag, getFlagActually(permissions, flag));
    }

    public static void hook(Residence residence) {
        if (nil(instance)) {
            instance = new PlaceholderSupport(residence);
            instance.hook();
        }
    }
}
