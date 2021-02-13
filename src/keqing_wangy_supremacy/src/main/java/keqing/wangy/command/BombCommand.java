package keqing.wangy.command;
import keqing.wangy.entities.Worm;
import keqing.wangy.enums.AttackType;

public class BombCommand implements Command {

    private final int x;
    private final int y;
    private String bombType;


    public BombCommand(int x,int y, AttackType type) {
        this.x = x;
        this.y = y;
        switch(type){
            case SNOWBALL:
                this.bombType = "snowball"; break;
            case BANANA:
            default:
                this.bombType = "banana";
        }
    }

    @Override
    public String render() {
        return String.format("%s %d %d", bombType, x, y);
    }
}
