package keqing.wangy.entities;

import com.google.gson.annotations.SerializedName;
import keqing.wangy.enums.Profession;

public class Worm {
    @SerializedName("id")
    public int id;

    @SerializedName("health")
    public int health;

    @SerializedName("position")
    public Position position;

    @SerializedName("diggingRange")
    public int diggingRange;

    @SerializedName("movementRange")
    public int movementRange;

    @SerializedName("profession")
    public Profession profession;
}
