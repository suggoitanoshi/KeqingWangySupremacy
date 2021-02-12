package keqing.wangy.entities;

import com.google.gson.annotations.SerializedName;

public class MyWorm extends Worm {
    @SerializedName("weapon")
    public Weapon weapon;

    @SerializedName("bananaBombs")
    public BananaBomb bananaBomb;

    @SerializedName("snowballs")
    public Snowball snowball;
}
