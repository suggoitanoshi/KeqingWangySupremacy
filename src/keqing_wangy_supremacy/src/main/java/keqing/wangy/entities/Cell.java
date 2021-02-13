package keqing.wangy.entities;

import java.io.Serial;

import com.google.gson.annotations.SerializedName;
import keqing.wangy.enums.CellType;

public class Cell {
    @SerializedName("x")
    public int x;

    @SerializedName("y")
    public int y;

    @SerializedName("type")
    public CellType type;

    @SerializedName("powerup")
    public PowerUp powerUp;

    @SerializedName("occupier")
    public Occupier occupier;
}
