package network.gameapi.games.onevsones;

import org.bukkit.entity.Player;

import network.gameapi.games.onevsones.kits.OneVsOneKit;

public class PrivateBattle {
    private Player challenger;
    private Player challenged;
    private OneVsOneKit kit = null;

    public PrivateBattle(Player challenger, Player challeneged, OneVsOneKit kit) {
        this.challenger = challenger;
        this.challenged = challeneged;
        this.kit = kit;
    }

    public Player getChallenger() {
        return challenger;
    }

    public void setChallenger(Player challenger) {
        this.challenger = challenger;
    }

    public Player getChallenged() {
        return challenged;
    }

    public void setChallenged(Player challenged) {
        this.challenged = challenged;
    }

    public OneVsOneKit getKit() {
        return kit;
    }

    public void setKit(OneVsOneKit kit) {
        this.kit = kit;
    }

    public void remove() {
        challenger = null;
        challenged = null;
        kit = null;
    }
}
