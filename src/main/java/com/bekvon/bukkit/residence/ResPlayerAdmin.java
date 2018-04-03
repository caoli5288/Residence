package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.residence.utils.BiFunctionRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

import static com.bekvon.bukkit.residence.$.nil;

public class ResPlayerAdmin implements PluginHelper.IExec {

    private final BiFunctionRegistry<CommandSender, Iterator<String>, Void> registry = new BiFunctionRegistry<>();

    {
        registry.register("setmaxres", (sender, itr) -> {
            Player p = Bukkit.getPlayerExact(itr.next());
            if (nil(p)) {
                sender.sendMessage("player not online");
                return null;
            }
            int nValue = Integer.parseInt(itr.next());
            ResidencePlayerBean resp = L2Pool.getResPlayer(p);
            resp.setMaxRes(nValue);
            $.saveAsync(resp);
            Residence.getInstance().getPlayerManager().getResidencePlayer(p).recountMaxRes();
            sender.sendMessage("done");
            return null;
        });
        registry.register("setmaxheight", (sender, itr) -> {
            Player p = Bukkit.getPlayerExact(itr.next());
            if (nil(p)) {
                sender.sendMessage("player not online");
                return null;
            }
            int nValue = Integer.parseInt(itr.next());
            ResidencePlayerBean resp = L2Pool.getResPlayer(p);
            resp.setMaxHeight(nValue);
            $.saveAsync(resp);
            sender.sendMessage("done");
            return null;
        });
        registry.register("setmaxweight", (sender, itr) -> {
            Player p = Bukkit.getPlayerExact(itr.next());
            if (nil(p)) {
                sender.sendMessage("player not online");
                return null;
            }
            int nValue = Integer.parseInt(itr.next());
            ResidencePlayerBean resp = L2Pool.getResPlayer(p);
            resp.setMaxWeight(nValue);
            $.saveAsync(resp);
            sender.sendMessage("done");
            return null;
        });
    }

    public void exec(CommandSender sender, List<String> list) {
        Iterator<String> itr = list.iterator();
        registry.handle(itr.next(), sender, itr);
    }
}
