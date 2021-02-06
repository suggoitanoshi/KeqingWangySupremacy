package keqing.wangy.command;

import keqing.wangy.enums.Direction;

public class ShootCommand implements Command {

    private Direction direction;

    public ShootCommand(Direction direction) {
        this.direction = direction;
    }

    @Override
    public String render() {
        return String.format("shoot %s", direction.name());
    }
}
