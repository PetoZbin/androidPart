package com.example.orienteering.retrofit.responseClasses.results;

import com.example.orienteering.retrofit.patternClasses.BasicResultPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class PageResultResponse extends CommonResponse {

    @SerializedName("data")
    private PageResultData data;

    public PageResultData getData() {
        return data;
    }

    public void setData(PageResultData data) {
        this.data = data;
    }
}
