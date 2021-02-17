package keqing.wangy;

import keqing.wangy.command.*;
import keqing.wangy.entities.*;
import keqing.wangy.enums.AttackType;
import keqing.wangy.enums.CellType;
import keqing.wangy.enums.Direction;
import keqing.wangy.enums.Profession;

import java.util.*;

public class Bot {

    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    public Bot(GameState gameState) {
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
        Command c = null;
        if(currentWorm.health < 70) c = goToPowerUp(); 
        if(c == null) c = serangMusuhTerdekat();
        return c;
    }

    // Get maximum value of an array

    private int getMin(int[] arr){
        int minValue = arr[0];
        for(int value: arr){
            if(value < minValue) minValue = value;
        }
        return minValue;
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
                if(cell.powerUp != null){
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

    private Command goToPowerUp(){
        Cell powerUp = getNearestPowerUp();
        if(powerUp == null) return null;
        return MoveTo(powerUp.x, powerUp.y);
    }

    private Cell[] getCellsAround(){
        Cell[] aroundWorm = new Cell[8];
        int k = 0;
        for(int i = currentWorm.position.y-1; i < currentWorm.position.y+2; i++){
            for(int j = currentWorm.position.x-1; j < currentWorm.position.x+2; j++){
                if(!isValidCoordinate(j, i) || (i == currentWorm.position.y && j == currentWorm.position.x)) continue;
                aroundWorm[k++] = gameState.map[i][j];
            }
        }
        return aroundWorm;
    }

    private Command MoveTo(int x, int y){
        // berusaha bergerak ke posisi (x,y)
        //if(currentWorm.position.x == x && currentWorm.position.y == y) return null;
        // cari sel di sekitar worm yang membutuhkan turn ter-sedikit untuk bergerak ke target
        Cell[] aroundWorm = getCellsAround();
        Cell leastResistance = null;
        Direction dir = resolveDirection(currentWorm.position, x, y);
        Direction cur;
        int cost, leastCost = Integer.MAX_VALUE;
        for(int i = 0; i < 8 && aroundWorm[i] != null; i++){
            cur = resolveDirection(currentWorm.position, aroundWorm[i]);
            if(aroundWorm[i].type == CellType.DEEP_SPACE) cost = Integer.MAX_VALUE-1;
            else{
                cost = 0;
                if(aroundWorm[i].type != CellType.AIR) cost++;
                if(dir.x != cur.x)
                    cost += (cur.x == -dir.x ? 2 : 1);
                if(dir.y != cur.y)
                    cost += (cur.y == -dir.y ? 2 : 1);
                if(aroundWorm[i].occupier != null)
                    cost+=2;
            }
            if(cost < leastCost){
                leastResistance = aroundWorm[i];
                leastCost = cost;
            }
        }
        switch(leastResistance.type){
            case LAVA:
            case AIR:
                return new MoveCommand(leastResistance.x, leastResistance.y);
            case DIRT:
                return new DigCommand(leastResistance.x, leastResistance.y);
            default:
                return new DoNothingCommand();
        }
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

    private boolean checkFriendInRadius(Position loc, int range){
        // lihat apakah ada teman di radius
        for(Worm worm: gameState.myPlayer.worms){
            if(radiusDistance(worm, loc) <= range) return true;
        }
        return false;
    }

    // To check if enemy is in attack or not (range)
    private boolean checkEnemyInRange(Worm enemy) {
        return (euclideanDistance(currentWorm, enemy) <= currentWorm.weapon.range);
    }

    private AttackType attackPriority() {
        if (currentWorm.profession == Profession.AGENT && currentWorm.bananaBomb.count > 0) return AttackType.BANANA;
        if (currentWorm.profession == Profession.TECHNOLOGIST && currentWorm.snowball.count > 0) return AttackType.SNOWBALL;
        return AttackType.SHOOT;
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, int x, int y){
        StringBuilder builder = new StringBuilder();

        int vert = y - a.y;
        int hori = x - a.x;

        if(vert < 0) builder.append('N');
        else if(vert > 0) builder.append('S');

        if(hori < 0) builder.append('W');
        else if(hori > 0) builder.append('E');
        return Direction.valueOf(builder.toString());
    }

    private Direction resolveDirection(Position a, Position b) {
        return resolveDirection(a, b.x, b.y);
    }

    private Direction resolveDirection(Position a, Cell b){
        return resolveDirection(a, b.x, b.y);
    }

    private Worm getNearestEnemy() {
        int[] enemiesDistance = new int[3];
        for (int i = 0; i < 3; i++) {
            if (opponent.worms[i].health > 0) {
                enemiesDistance[i] = euclideanDistance(currentWorm, opponent.worms[i]);
            }
            else {
                enemiesDistance[i] = Integer.MAX_VALUE;
            }
        }
        int maxDistance = getMin(enemiesDistance);
        return opponent.worms[findIndex(enemiesDistance, maxDistance)];
    }

    private boolean checkBerkasTembak(Direction dir){
        // cek apakah "ray" dari worm sekarang ke arah dir ada halangan
        int x = currentWorm.position.x, y = currentWorm.position.y;
        boolean safe = true;
        do{
            // "tembak" berkas ke arah dir
            if(dir.x != 0) x += dir.x;
            if(dir.y != 0) y += dir.y;
            // kalau berkas nabrak sesuatu yang bukan air, berarti gak bisa nembak
            safe = (gameState.map[y][x].type == CellType.AIR);
            safe &= !(gameState.map[y][x].occupier != null && gameState.map[y][x].occupier.playerId == gameState.myPlayer.id);
        }
        while(isValidCoordinate(x, y) &&
        euclideanDistance(currentWorm.position.x, currentWorm.position.y, x, y) <= currentWorm.weapon.range &&
        safe &&
        (gameState.map[y][x].occupier == null));
        return safe;
    }

    private boolean checkInAttackDirection(Worm enemy){
        // cek apakah enemy ada di salah satu dari 8 arah
        return (Math.abs(enemy.position.x - currentWorm.position.x) == Math.abs(enemy.position.y - currentWorm.position.y)) ||
        (enemy.position.x == currentWorm.position.x) || (enemy.position.y == currentWorm.position.y);
    }

    private Command serangMusuhTerdekat(){
        //pendekatan yang cukup baik, cukup dekati sampai masuk range dulu, nanti adjust
        Worm enemy = getNearestEnemy();
        if(gameState.map[currentWorm.position.y][currentWorm.position.x].type == CellType.LAVA) MoveTo(enemy.position.x, enemy.position.y);
        AttackType type = attackPriority();
        if(type != AttackType.SHOOT){
            if(
                (currentWorm.snowball != null && !checkFriendInRadius(enemy.position, currentWorm.snowball.freezeRadius) && enemy.roundsUnfroze == 0) ||
                (currentWorm.bananaBomb != null && !checkFriendInRadius(enemy.position, currentWorm.bananaBomb.damageRadius))
            )
                return new BombCommand(enemy.position.x, enemy.position.y, type);
        }
        if(!checkEnemyInRange(enemy)) return MoveTo(enemy.position.x, enemy.position.y);
        if(checkInAttackDirection(enemy)){
            Direction dir = resolveDirection(currentWorm.position, enemy.position);
            if(checkBerkasTembak(dir)) return new ShootCommand(dir);
            else return MoveTo(enemy.position.x, enemy.position.y); // deketin lagi
        } 
        else return MoveTo(enemy.position.x, enemy.position.y);
    }
}
