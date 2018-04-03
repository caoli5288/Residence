package com.bekvon.bukkit.residence.containers;

import com.avaje.ebean.EbeanServer;
import com.bekvon.bukkit.residence.L2Pool;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.ResidencePlayerBean;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.bekvon.bukkit.residence.$.nil;

public class ResidencePlayer {

    private String userName = null;
    private Player player = null;
    private OfflinePlayer ofPlayer = null;
    private UUID uuid = null;

    private Set<ClaimedResidence> ResidenceList = new HashSet<ClaimedResidence>();
    private ClaimedResidence mainResidence = null;

    private PlayerGroup groups = null;

    private int maxRes = -1;
    private int maxRents = -1;
    private int maxSubzones = -1;
    private int maxSubzoneDepth = -1;

    private int maxValue = 9999;

    private ResidencePlayerBean bean;

    public ResidencePlayer(OfflinePlayer off) {
        if (off == null)
            return;
        this.uuid = off.getUniqueId();
        this.userName = off.getName();
        Residence.getInstance().addOfflinePlayerToChache(off);
        this.updatePlayer();
        this.recalculatePermissions();
    }

    public ResidencePlayer(Player player) {
        if (player == null)
            return;
        Residence.getInstance().addOfflinePlayerToChache(player);
        this.updatePlayer(player);
        this.recalculatePermissions();
    }

    public boolean isOnline() {
        this.updatePlayer();
        if (this.player != null && this.player.isOnline())
            return true;
        return false;
    }

    public ResidencePlayer(String userName, UUID uuid) {
        this.userName = userName;
        this.uuid = uuid;
        if (this.isOnline())
            recalculatePermissions();
    }

    public ResidencePlayer(String userName) {
        this.userName = userName;
        if (this.isOnline())
            recalculatePermissions();
    }

    public void setMainResidence(ClaimedResidence res) {
        if (mainResidence != null)
            mainResidence.setMainResidence(false);
        mainResidence = res;
    }

    public ClaimedResidence getMainResidence() {
        if (mainResidence == null) {
            for (ClaimedResidence one : ResidenceList) {
                if (one == null)
                    continue;
                if (one.isMainResidence()) {
                    mainResidence = one;
                    return mainResidence;
                }
            }
            for (String one : Residence.getInstance().getRentManager().getRentedLands(this.userName)) {
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(one);
                if (res != null) {
                    mainResidence = res;
                    return mainResidence;
                }
            }
            for (ClaimedResidence one : ResidenceList) {
                if (one == null)
                    continue;
                mainResidence = one;
                return mainResidence;
            }
        }
        return mainResidence;
    }

    public void recalculatePermissions() {
        getGroup();
        recountMaxRes();
        recountMaxRents();
        recountMaxSubzones();
    }

    public void recountMaxRes() {
        if (this.getGroup() != null)
            this.maxRes = this.getGroup().getMaxZones();
        this.maxRes = this.maxRes == -1 ? maxValue : this.maxRes;

        if (player != null && player.isOnline()) {
            if (this.player.isPermissionSet("residence.max.res.unlimited")) {
                this.maxRes = maxValue;
                return;
            }
        } else if (ofPlayer != null) {
            if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.res.unlimited", Residence.getInstance().getConfigManager().getDefaultWorld())) {
                this.maxRes = maxValue;
                return;
            }
        }

        for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxResCount(); i++) {
            if (player != null && player.isOnline()) {
                if (this.player.isPermissionSet("residence.max.res." + i))
                    this.maxRes = i;
            } else if (ofPlayer != null) {
                if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.res." + i, Residence.getInstance().getConfigManager().getDefaultWorld()))
                    this.maxRes = i;
            }
        }

        ResidencePlayerBean b = L2Pool.INSTANCE.getResPlayer(player);
        maxRes += b.getMaxRes();
    }

    public void recountMaxRents() {
        if (player != null) {
            if (this.player.isPermissionSet("residence.max.rents.unlimited")) {
                this.maxRents = maxValue;
                return;
            }
        } else {
            if (ofPlayer != null)
                if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.rents.unlimited", Residence.getInstance().getConfigManager().getDefaultWorld())) {
                    this.maxRents = maxValue;
                    return;
                }
        }
        for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxRentCount(); i++) {
            if (player != null) {
                if (this.player.isPermissionSet("residence.max.rents.unlimited" + i))
                    this.maxRents = i;
            } else {
                if (ofPlayer != null)
                    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.rents." + i, Residence.getInstance().getConfigManager().getDefaultWorld()))
                        this.maxRents = i;
            }
        }

        int m = this.getGroup().getMaxRents();
        m = m == -1 ? maxValue : m;
        if (this.maxRents < m)
            this.maxRents = m;
    }

    public int getMaxRents() {
        recountMaxRents();
        return this.maxRents;
    }

    public void recountMaxSubzones() {
        if (player != null) {
            if (this.player.isPermissionSet("residence.max.subzones.unlimited")) {
                this.maxSubzones = maxValue;
                return;
            }
        } else {
            if (ofPlayer != null)
                if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzones.unlimited", Residence.getInstance().getConfigManager().getDefaultWorld())) {
                    this.maxSubzones = maxValue;
                    return;
                }
        }
        for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxSubzonesCount(); i++) {
            if (player != null) {
                if (this.player.isPermissionSet("residence.max.subzones." + i))
                    this.maxSubzones = i;
            } else {
                if (ofPlayer != null)
                    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzones." + i, Residence.getInstance().getConfigManager().getDefaultWorld()))
                        this.maxSubzones = i;
            }
        }

        int m = this.getGroup().getMaxSubzones();
        m = m == -1 ? maxValue : m;
        if (this.maxSubzones < m)
            this.maxSubzones = m;
    }

    public int getMaxSubzones() {
        recountMaxSubzones();
        return this.maxSubzones;
    }

    public void recountMaxSubzoneDepth() {
        if (player != null) {
            if (this.player.isPermissionSet("residence.max.subzonedepth.unlimited")) {
                this.maxSubzoneDepth = maxValue;
                return;
            }
        } else {
            if (ofPlayer != null)
                if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzonedepth.unlimited", Residence.getInstance().getConfigManager().getDefaultWorld())) {
                    this.maxSubzoneDepth = maxValue;
                    return;
                }
        }
        for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxSubzoneDepthCount(); i++) {
            if (player != null) {
                if (this.player.isPermissionSet("residence.max.subzonedepth." + i))
                    this.maxSubzoneDepth = i;
            } else {
                if (ofPlayer != null)
                    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzonedepth." + i, Residence.getInstance().getConfigManager().getDefaultWorld()))
                        this.maxSubzoneDepth = i;
            }
        }

        int m = this.getGroup().getMaxSubzoneDepth();
        m = m == -1 ? maxValue : m;
        if (this.maxSubzoneDepth < m)
            this.maxSubzoneDepth = m;
    }

    public int getMaxSubzoneDepth() {
        recountMaxSubzoneDepth();
        return this.maxSubzoneDepth;
    }

    public int getMaxRes() {
        recountMaxRes();
        PermissionGroup g = getGroup();
        if (this.maxRes < g.getMaxZones()) {
            return g.getMaxZones();
        }
        return this.maxRes;
    }

    public PermissionGroup getGroup() {
        updatePlayer();
        return getGroup(this.player != null ? player.getWorld().getName() : Residence.getInstance().getConfigManager().getDefaultWorld());
    }

    public PermissionGroup getGroup(String world) {
        if (groups == null)
            groups = new PlayerGroup(this);
        groups.updateGroup(world, false);
        PermissionGroup group = groups.getGroup(world);
        if (group == null)
            group = Residence.getInstance().getPermissionManager().getDefaultGroup();
        return group;
    }

    public ResidencePlayer updatePlayer(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.userName = player.getName();
        this.ofPlayer = player;
        return this;
    }

    private void updatePlayer() {
        player = Bukkit.getPlayer(this.uuid);
        if (player != null)
            updatePlayer(player);
        if (player != null && player.isOnline())
            return;
        if (this.uuid != null && Bukkit.getPlayer(this.uuid) != null) {
            player = Bukkit.getPlayer(this.uuid);
            this.userName = player.getName();
            return;
        }

        if (this.userName != null) {
            player = Bukkit.getPlayer(this.userName);
        }
        if (player != null) {
            this.userName = player.getName();
            this.uuid = player.getUniqueId();
            this.ofPlayer = player;
            return;
        }
        if (this.player == null && ofPlayer == null)
            ofPlayer = Residence.getInstance().getOfflinePlayer(userName);
        if (ofPlayer != null) {
            this.userName = ofPlayer.getName();
            this.uuid = ofPlayer.getUniqueId();
            return;
        }
    }

    public void addResidence(ClaimedResidence residence) {
        if (residence == null)
            return;
        // Exclude subzones
        if (residence.isSubzone())
            return;
        residence.getPermissions().setOwnerUUID(uuid);
        if (this.userName != null)
            residence.getPermissions().setOwnerLastKnownName(userName);
        this.ResidenceList.add(residence);
    }

    public void removeResidence(ClaimedResidence residence) {
        if (residence == null)
            return;
        boolean rem = this.ResidenceList.remove(residence);
        // in case its fails to remove, double check by name
        if (rem == false) {
            Iterator<ClaimedResidence> iter = this.ResidenceList.iterator();
            while (iter.hasNext()) {
                ClaimedResidence one = iter.next();
                if (one.getName().equalsIgnoreCase(residence.getName())) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    public int getResAmount() {
        int i = 0;
        for (ClaimedResidence one : ResidenceList) {
            if (one.isSubzone())
                continue;
            i++;
        }
        return i;
    }

    public List<ClaimedResidence> getResList() {
        List<ClaimedResidence> ls = new ArrayList<ClaimedResidence>();
        ls.addAll(ResidenceList);
        return ls;
    }

    public String getPlayerName() {
        this.updatePlayer();
        return userName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        this.updatePlayer();
        return player;
    }
}
