package com.bekvon.bukkit.residence;

import static java.util.concurrent.CompletableFuture.runAsync;

public class $ {

    public static boolean nil(Object input) {
        return input == null;
    }

    public static void saveAsync(Object any) {
        runAsync(() -> Residence.getDataServer().save(any));
    }
}
