package keqing.wangy.entities;

import com.google.gson.annotations.SerializedName;

public class MyPlayer {
    @SerializedName("id")
    public int id;

    @SerializedName("score")
    public int score;

    @SerializedName("health")
    public int health;

    @SerializedName("worms")
    public MyWorm[] worms;

    @SerializedName("remainingWormSelection")
    public int remainingSelect;
}
