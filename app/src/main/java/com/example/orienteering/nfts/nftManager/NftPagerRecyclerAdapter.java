package com.example.orienteering.nfts.nftManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.orienteering.R;
import com.example.orienteering.retrofit.responseClasses.tokens.AddressNftsData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NftPagerRecyclerAdapter extends RecyclerView.Adapter<NftPagerRecyclerAdapter.NftViewHolder>{

    List<AddressNftsData> itemList = new ArrayList<>();
    //Boolean isLoading;

    public NftPagerRecyclerAdapter(List<AddressNftsData> itemList, Boolean isLoading) {
        this.itemList = itemList;
       // this.isLoading = isLoading;     //vtedy ukaz vo viewholderi loading wheel
    }

    public NftPagerRecyclerAdapter() {
    }

    public void setItemList(List<AddressNftsData> itemList) {
        this.itemList = itemList;
    }

//    public void setLoading(Boolean loading) {
//        isLoading = loading;
//    }

    public static class NftViewHolder extends RecyclerView.ViewHolder{

        private ImageView nftImage;

        public NftViewHolder(@NonNull View itemView) {
            super(itemView);

            nftImage = itemView.findViewById(R.id.nft_mage_disp);

        }

        public void fetchImage(String imageUrl){

            // https://stackoverflow.com/a/39047157 - zdroj pre placeholder
//            Picasso.get().load(imageUrl)
//                    .resizeDimen(R.dimen.nft_width,R.dimen.nft_height)
//                    .onlyScaleDown()
//                    .error(R.drawable.no_fts_image)
//                    .placeholder(R.drawable.custom_spinner).into(nftImage);

//            if (isLoading){
//
//                Picasso.get().load(R.drawable.custom_spinner).into(nftImage);   //nacitaj loading wheel
//            }
//            else {  //nacitaj obrazok

                Picasso.get().load(imageUrl)
                        .resizeDimen(R.dimen.nft_width,R.dimen.nft_height)
                        .error(R.drawable.no_fts_image)
                        .placeholder(R.drawable.custom_spinner).into(nftImage);
            }


    }


    @NonNull
    @Override
    public NftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nft_image_layout, parent, false);

        return new NftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NftViewHolder holder, int position) {

        if (position != RecyclerView.NO_POSITION){

            holder.fetchImage(itemList.get(position).getImageUrl());
        }


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
