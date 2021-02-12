package keqing.wangy;

import keqing.wangy.command.*;
import keqing.wangy.entities.*;
import keqing.wangy.enums.CellType;
import keqing.wangy.enums.Direction;
import keqing.wangy.enums.PowerUpType;
import keqing.wangy.enums.Profession;

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

    // Get maximum value of an array
    private int getMax(int[] arr) {
        int maxValue = arr[0];
        for (int value : arr) {
            if (value > maxValue) maxValue = value;
        }
        return maxValue;
    }

    // Get index of a value
    private int findIndex(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) return i;
        }
        return -999;
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

    private Command goToPowerUp(Cell powerUp){
        if(currentWorm.position.x == powerUp.x && currentWorm.position.y == powerUp.y) return null;
        Cell nearestPU = getNearestPowerUp();
        if(nearestPU == null) return null;
        return MoveTo(powerUp);
    }

    private Cell[] getCellsAround(){
        Cell[] aroundWorm = new Cell[8];
        int k = 0;
        for(int i = currentWorm.position.y-1; i < currentWorm.position.y+2; i++){
            for(int j = currentWorm.position.x-1; j < currentWorm.position.x+2; j++){
                aroundWorm[k++] = gameState.map[i][j];
            }
        }
        return aroundWorm;
    }

    private Command MoveTo(Cell cell){
        if(currentWorm.position.x == cell.x && currentWorm.position.y == cell.y) return null;
        Cell[] aroundWorm = getCellsAround();
        Cell leastResistance = null;
        Direction dir = resolveDirection(currentWorm.position, cell);
        Direction cur;
        int cost = Integer.MAX_VALUE, lastCost;
        for(int i = 0; i < 8 && aroundWorm[i] != null; i++){
            leastResistance = aroundWorm[i];
            cur = resolveDirection(currentWorm.position, aroundWorm[i]);
            lastCost = cost;
            if(aroundWorm[i].type == CellType.DEEP_SPACE) cost = Integer.MAX_VALUE-1;
            else{
                if(dir.x != dir.x) cost++;
                if(dir.y != cur.y) cost++;
            }
            if(cost < lastCost) leastResistance = aroundWorm[i];
        }
        switch(leastResistance.type){
            case AIR:
                return new MoveCommand(leastResistance.x, leastResistance.y);
            case DIRT:
                return new DigCommand(leastResistance.x, leastResistance.y);
            case DEEP_SPACE:
            default:
                return new DoNothingCommand();
        }
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

    private int euclideanDistance(Worm a, Worm b) {
        return euclideanDistance(a.position.x, a.position.y, b.position.x, b.position.y);
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
        if (currentWorm.profession == Profession.AGENT) return "banana";
        if (currentWorm.profession == Profession.TECHNOLOGIST) return "snowball";
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

    private Direction resolveDirection(Position a, Cell b){
        StringBuilder builder = new StringBuilder();

        int vert = b.y - a.y;
        int hori = b.x - a.x;

        if(vert < 0) builder.append('N');
        else if(vert > 0) builder.append('S');

        if(hori < 0) builder.append('W');
        else if(hori > 0) builder.append('E');

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

    private Worm getNearestEnemy() {
        int[] enemiesDistance = new int[3];
        for (int i = 0; i < 3; i++) {
            if (opponent.worms[i].health > 0) {
                enemiesDistance[i] = euclideanDistance(currentWorm, opponent.worms[i]);
            }
            else {
                enemiesDistance[i] = -999;
            }
        }
        int maxDistance = getMax(enemiesDistance);
        return opponent.worms[findIndex(enemiesDistance, maxDistance)];
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

    private boolean checkBerkasTembak(Direction dir){
        int x = currentWorm.position.x, y = currentWorm.position.y;
        boolean safe = true;
        do{
            // "tembak" berkas ke arah dir
            x += dir.x;
            y += dir.y;
            // kalau berkas nabrak sesuatu yang bukan air, berarti gak bisa nembak
            if(gameState.map[y][x].type != CellType.AIR) safe = false;
        }
        while(euclideanDistance(currentWorm.position.x, currentWorm.position.y, x, y) < currentWorm.weapon.range && safe);
        return safe;
    }

    private boolean checkInAttackDirection(Worm enemy){
        return (Math.abs(enemy.position.x - currentWorm.position.x) == Math.abs(enemy.position.y - currentWorm.position.y) ||
        (enemy.position.x == currentWorm.position.x) || (enemy.position.y == currentWorm.position.y));
    }

    private Command serangMusuhTerdekat(){
        // pendekatan yang cukup baik, cukup dekati sampai masuk range dulu, nanti adjust
        Worm enemy = getNearestEnemy();
        if(euclideanDistance(currentWorm, enemy) > currentWorm.weapon.range) return MoveTo(gameState.map[enemy.position.y][enemy.position.x]);
        if(checkInAttackDirection(enemy)){
            Direction dir = resolveDirection(currentWorm.position, enemy.position);
            if(checkBerkasTembak(dir)) return new ShootCommand(dir);
            else return MoveTo(gameState.map[enemy.position.y][enemy.position.x]); // deketin lagi
        } 
        return new DoNothingCommand();
    }
}
