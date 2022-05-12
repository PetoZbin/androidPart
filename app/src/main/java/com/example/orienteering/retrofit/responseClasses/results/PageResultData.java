package com.example.orienteering.retrofit.responseClasses.results;

import com.example.orienteering.retrofit.patternClasses.BasicResultPattern;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PageResultData {

    @SerializedName("competitions")
    private List<BasicResultPattern> results;

    @SerializedName("pageNum")
    private Integer pageNum;

    @SerializedName("allComps")
    private Integer allComps;

    @SerializedName("allPages")
    private Integer allPages;


    public List<BasicResultPattern> getResults() {
        return results;
    }

    public void setResults(List<BasicResultPattern> results) {
        this.results = results;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getAllComps() {
        return allComps;
    }

    public void setAllComps(Integer allComps) {
        this.allComps = allComps;
    }

    public Integer getAllPages() {
        return allPages;
    }

    public void setAllPages(Integer allPages) {
        this.allPages = allPages;
    }
}
