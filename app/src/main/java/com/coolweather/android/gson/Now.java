package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperatur;
    @SerializedName("cond_txt")
    public String info;
}
