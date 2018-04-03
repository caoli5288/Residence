package com.bekvon.bukkit.residence;

import com.avaje.ebean.EbeanServer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static com.bekvon.bukkit.residence.$.nil;

public enum L2Pool {

    INSTANCE;

    private final Cache<String, Object> pool = CacheBuilder.newBuilder().build();

    public static <T> T b(String namespace, Supplier<T> factory) {
        try {
            return (T) INSTANCE.pool.get(namespace, factory::get);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void remove(String key) {
        INSTANCE.pool.invalidate(key);
    }

    public static CompletableFuture<ResidencePlayerBean> getResPlayerFuture(Player p) {
        return b(p.getUniqueId() + ":resplayer", () -> CompletableFuture.supplyAsync(() -> {
            EbeanServer db = Residence.getInstance().getDataServer();
            ResidencePlayerBean result = db.find(ResidencePlayerBean.class, p.getUniqueId());
            if (nil(result)) {
                result = db.createEntityBean(ResidencePlayerBean.class);
                result.setId(p.getUniqueId());
                result.setName(p.getName());
            }
            return result;
        }));
    }

    public static ResidencePlayerBean getResPlayer(Player p) {
        try {
            return getResPlayerFuture(p).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void quit(Player p) {
        String ns = p.getUniqueId() + ":";
        INSTANCE.pool.asMap().keySet().removeIf(key -> key.startsWith(ns));
    }
}
