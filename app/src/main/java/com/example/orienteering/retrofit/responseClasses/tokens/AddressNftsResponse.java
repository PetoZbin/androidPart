package com.example.orienteering.retrofit.responseClasses.tokens;

import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressNftsResponse extends CommonResponse {

    @SerializedName("data")
    private List<AddressNftsData> data;

    public List<AddressNftsData> getData() {
        return data;
    }

    public void setData(List<AddressNftsData> data) {
        this.data = data;
    }
}
