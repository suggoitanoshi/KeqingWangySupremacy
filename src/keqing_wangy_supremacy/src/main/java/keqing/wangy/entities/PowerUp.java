package keqing.wangy.entities;

import com.google.gson.annotations.SerializedName;
import keqing.wangy.enums.PowerUpType;

public class PowerUp {
    @SerializedName("type")
    public PowerUpType type;

    @SerializedName("value")
    public int value;
}
