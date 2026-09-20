package net.redct.client.utils;

import net.redct.client.data.Location;

public enum PlayerInfo {
    INSTANCE;

    private Location currentLocation= Location.UNKNOWN;

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location loc) {
        this.currentLocation = loc;
    }
}