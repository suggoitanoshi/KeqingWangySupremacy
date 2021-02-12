package keqing.wangy;

import keqing.wangy.command.*;
import keqing.wangy.entities.*;
import keqing.wangy.enums.CellType;
import keqing.wangy.enums.Direction;
import keqing.wangy.enums.PowerUpType;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    public Command run() {

        // cari powerup terdekat
        Cell nearestPowerUp = getNearestPowerUp();
        // kalau ada, jalan ke sana
        // cari jalannya
        // prioritas: sumbu y, baru sumbu x
        // ilustrasi
        /* H: Health Pack, P: Player
        *  .H.
        *  .P.
        *  ...
        *  H.y - P.y = -1
        * --------------
        *  ...
        *  .P.
        *  .H.
        *  H.y - P.y = 1
        * 
        * dst.
        */
        if(nearestPowerUp != null){
            // cari jarak y dan jarak x nya
            int dy = nearestPowerUp.y - currentWorm.position.y;
            int dx = nearestPowerUp.x - currentWorm.position.x;
            // cari tandanya, bagi dengan besar asli tanpa tanda
            // cth. untuk x = -5, -5/5 = -1 (tandanya -)
            // untuk x = 5, 5/5 = 1 (tandanya +)
            if(dy != 0) dy = dy/Math.abs(dy);
            if(dx != 0) dx = dx/Math.abs(dx);

            int nextY = currentWorm.position.y + dy;
            int nextX = currentWorm.position.x + dx;
            if(gameState.map[nextY][nextX].type == CellType.AIR) return new MoveCommand(nextX, nextY);
            else if(gameState.map[nextY][nextX-1].type == CellType.AIR) return new MoveCommand(nextX-1, nextY);
            else if(gameState.map[nextY-1][nextX].type == CellType.AIR) return new MoveCommand(nextX, nextY-1);
            else if(gameState.map[nextY][nextX].type == CellType.DIRT) return new DigCommand(nextX, nextY);
            else if(gameState.map[nextY][nextX-1].type == CellType.DIRT) return new DigCommand(nextX-1, nextY);
            else if(gameState.map[nextY-1][nextX].type == CellType.DIRT) return new DigCommand(nextX, nextY-1);
        }

        // TODO: implementasi greedy yang lain
        Worm enemyWorm = getFirstWormInRange();
        if (enemyWorm != null) {
            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new ShootCommand(direction);
        }

        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        int cellIdx = random.nextInt(surroundingBlocks.size());

        Cell block = surroundingBlocks.get(cellIdx);
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }

        return new DoNothingCommand();
    }

    private Cell getNearestPowerUp(){
        Cell nearestPowerUp = null;
        for(Cell[] row: gameState.map){
            for(Cell cell: row){
                if(cell.powerUp != null && cell.powerUp.type == PowerUpType.HEALTH_PACK){
                    if(nearestPowerUp != null){
                        if(euclideanDistance(currentWorm, cell) < euclideanDistance(currentWorm, nearestPowerUp)){
                            nearestPowerUp = cell;
                        }
                    }
                    else{
                        nearestPowerUp = cell;
                    }
                }
            }
        }
        return nearestPowerUp;
    }

    private Worm getFirstWormInRange() {

        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                return enemyWorm;
            }
        }

        return null;
    }

    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private int euclideanDistance(Worm a, Cell b){
        return euclideanDistance(a.position.x, a.position.y, b.x, b.y);
    }

    private int radiusDistance(int aX, int aY, int bX, int bY) {
        return (int) Math.ceil(Math.sqrt(Math.pow(aX-bX, 2) + Math.pow(aY - bY, 2)));
    }

    private int radiusDistance(Worm a, Position b) {
        return radiusDistance(a.position.x, a.position.y, b.x, b.y);
    }

    // To check if enemy is in attack range or not (bomb)
    // locX : position (x) of bomb thrown, locY : same
    // attRange : range of the bomb
    private boolean checkEnemyInRadius(Position locDamage, int attRange) {
        for (Worm enemyWorm : opponent.worms) {
            if (radiusDistance(enemyWorm, locDamage) <= attRange) return true;
        }
        return false;
    }

    // To check if enemy is in attack or not (range)
    private boolean checkEnemyInRange(Worm enemy) {
        if (getFirstWormInRange() != enemy) return false;
        return true;
    }

    // TODO: ngecek punya sisa brpa, tapi how to access da shiet
    private String attackPriority() {
        if (currentWorm.id == 2) return "banana";
        if (currentWorm.id == 3) return "snowball";
        return "shoot";
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }

    private Worm getNearestFriend()
    {
        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm friendWorm : gameState.myPlayer.worms) {
            String friendPosition = String.format("%d_%d", friendWorm.position.x, friendWorm.position.y);
            if (cells.contains(friendPosition)) {
                return friendWorm;
            }
        }

        return null;
    }

    private List<Cell> checkSafe(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    if (gameState.map[j][i].type == CellType.AIR )
                    {
                        cells.add(gameState.map[j][i]);
                    }
                }
            }
        }

        return cells;
    }

    
}
