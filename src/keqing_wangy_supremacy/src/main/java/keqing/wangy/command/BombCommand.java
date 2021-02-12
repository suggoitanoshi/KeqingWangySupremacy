package keqing.wangy.command;
import keqing.wangy.entities.Worm;

public class BombCommand implements Command {

    private final int x;
    private final int y;
    private String bombType;


    public BombCommand(int x,int y, int currentWormId) {
        this.x = x;
        this.y = y;
        if (currentWormId == 2)
        {
            this.bombType = "banana";
        }
        else if (currentWormId == 3)
        {
            this.bombType = "snowball";
        }
    }

    @Override
    public String render() {
        return String.format("%s %d %d", bombType, x, y);
    }
}
