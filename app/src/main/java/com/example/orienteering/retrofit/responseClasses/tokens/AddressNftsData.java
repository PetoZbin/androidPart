package com.example.orienteering.retrofit.responseClasses.tokens;

import com.google.gson.annotations.SerializedName;

public class AddressNftsData {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("image")        //image url
    private String imageUrl;

    @SerializedName("token_id")
    private String tokenId;

    @SerializedName("token_address")    //na ktorej adrese bezi smart kontrakt, kde je minovany token
    private String tokenAddress;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }
}
