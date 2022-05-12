package com.example.orienteering.competition.competitionPreview.otherPreviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentPrizePreviewDialogBinding;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.squareup.picasso.Picasso;

import java.util.List;


public class PrizePreviewDialogFragment extends DialogFragment implements View.OnClickListener{

    private String img_url; //nft adresa, kde je uploadnuty obrazok - moralis brana html - ipfs
    private String prizeName;

    private FragmentPrizePreviewDialogBinding binding;
    private Dialog dialog;

    public PrizePreviewDialogFragment(String nftName, String nftImgUrl) {
        this.img_url = nftImgUrl;
        this.prizeName = nftName;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_prize_preview_dialog,null,false);
        dialogBuilder.setView(binding.getRoot());

        binding.prizePrevCloseBtn.setOnClickListener(this);


        binding.prizePrevNftNameText.setText(this.prizeName);

        fetchImage(img_url);

        dialog = dialogBuilder.create();
        return dialog;
    }

    private void fetchImage(String img_url){

        //picasso kniznica na natiahnutie obrazka zo siete
        Picasso.get().load(img_url)
                .resizeDimen(R.dimen.nft_width,R.dimen.nft_height)
                .error(R.drawable.no_fts_image)
                .placeholder(R.drawable.custom_spinner).into(binding.prizePrevImage);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }



    @Override
    public void onClick(View v) {

        if (v.equals(binding.prizePrevCloseBtn)){
            dialog.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.80);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.70);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corners);
    }
}