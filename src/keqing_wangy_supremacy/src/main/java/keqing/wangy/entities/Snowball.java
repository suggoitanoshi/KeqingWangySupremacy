package keqing.wangy.entities;

import com.google.gson.annotations.SerializedName;

public class Snowball {
  @SerializedName("range")
  public int range;

  @SerializedName("count")
  public int count;

  @SerializedName("freezeRadius")
  public int freezeRadius;
}
