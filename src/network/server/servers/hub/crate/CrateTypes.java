package network.server.servers.hub.crate;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public enum CrateTypes {
    VOTING("Voting"),
    PREMIUM("Premium");

    private String display;
    private String name;
    private Beacon beacon;

    CrateTypes(String display) {
        this.display = display;
        this.name = display.replace(" ", "_").toLowerCase();
    }

    public String getDisplay() {
        return this.display;
    }

    public String getName() {
        return this.name;
    }

    public Beacon getBeacon() {
        return this.beacon;
    }

    public void setBeacon(Block block, Vector vector) {
        this.beacon = new Beacon(getDisplay() + " Crate&8 (&7Click&8)", this, block, vector);
    }
}
