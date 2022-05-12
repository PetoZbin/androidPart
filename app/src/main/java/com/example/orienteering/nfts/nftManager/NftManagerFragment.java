package com.example.orienteering.nfts.nftManager;

import android.media.Image;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentNftManagerBinding;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.nfts.nftManager.responses.NftFetchFailResponse;
import com.example.orienteering.retrofit.responseClasses.tokens.AddressNftsData;
import com.example.orienteering.retrofit.responseClasses.tokens.AddressNftsResponse;
import com.example.orienteering.userAccess.onboarding.LoginViewModel;
import com.example.orienteering.userAccess.onboarding.responses.LoginSuccessResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class NftManagerFragment extends Fragment implements View.OnClickListener{

    public static final String SERIALIZED_NFT = "serializedNft";

    private FragmentNftManagerBinding nftManagerBinding;
    private NftManagerViewModel nftManagerViewModel;

    private ViewPager2 nftViewpager;
    private NftPagerRecyclerAdapter adapter;

    private ImageButton leftBtn;
    private ImageButton rightBtn;
    private ImageView loadingImage;
    private ImageView noNftImage;
    private Button pickNftButton;

    private int current_picked_index;

    public NftManagerFragment() {
        // Required empty public constructor
    }

    public static NftManagerFragment newInstance(String param1, String param2) {
        NftManagerFragment fragment = new NftManagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        nftManagerBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_nft_manager,container,false);
        nftManagerBinding.setLifecycleOwner(this);
        nftManagerViewModel = new ViewModelProvider(this).get(NftManagerViewModel.class);
        nftManagerBinding.setNftManagerViewModel(nftManagerViewModel);

        //initNftViewPager(new ArrayList<>());    //inicializuj viewpager
        //setLoadingPage();   // uz nepouzivam itemlist ma jednu polozku s loading wheel


        loadingImage = nftManagerBinding.nftManLoadingSpinner;
        loadingImage.setVisibility(View.GONE);
        noNftImage = nftManagerBinding.nftManNoNfts;
        noNftImage.setVisibility(View.GONE);
        nftViewpager = nftManagerBinding.nftImagePager;
        nftViewpager.setVisibility(View.GONE);

        nftManagerViewModel.getNftsList().observe(getViewLifecycleOwner(), new Observer<List<AddressNftsData>>() {
            @Override
            public void onChanged(List<AddressNftsData> response) {
                // prisla odpoved servera - nft list
                onNftsFetched(response);
            }
        });

        // fetch nfts using server calling moralis service

        nftManagerViewModel.getPickedAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String response) {
                //po uspesnom prihlaseni pride response, ktoru observujem
                nftManagerViewModel.fetchNfts();
            }
        });

        nftManagerViewModel.getFailResponse().observe(getViewLifecycleOwner(), new Observer<NftFetchFailResponse>() {
            @Override
            public void onChanged(NftFetchFailResponse response) {

                onNftsFetchFailed(response);    //nespesne dotiahnutie - error message
            }
        });

        nftManagerViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {  //true = retrofit nacitava, false = nenacitava

                if (isLoading){

                    nftViewpager.setVisibility(View.GONE);
                    noNftImage.setVisibility(View.GONE);
                    loadingImage.setVisibility(View.VISIBLE);
                    nftManagerBinding.nftManNoNftsLabel.setVisibility(View.GONE);
                    nftManagerBinding.nftManNoNfts.setVisibility(View.GONE);
                }
                else {

                    loadingImage.setVisibility(View.GONE);
                    nftViewpager.setVisibility(View.VISIBLE);
                }
            }
        });

        //nftManagerViewModel.fetchNfts();



        leftBtn = nftManagerBinding.nftManLeftBtn;
        leftBtn.setOnClickListener(this);
        leftBtn.setVisibility(View.GONE);

        rightBtn = nftManagerBinding.nftManRightBtn;
        rightBtn.setVisibility(View.GONE);
        rightBtn.setOnClickListener(this);

        pickNftButton = nftManagerBinding.pickNftBtnBtn;
        pickNftButton.setEnabled(false);
        pickNftButton.setOnClickListener(this);

        nftManagerViewModel.checkLogged(UserDatabase.getInstance(getContext()));


        return nftManagerBinding.getRoot();
    }

    private void initNftViewPager(List<AddressNftsData> itemList){  // inicializacia

        adapter = new NftPagerRecyclerAdapter();
        adapter.setItemList(itemList);

        nftViewpager.setUserInputEnabled(true);    //neaktualne - zmena UI-> prepinanie potiahnutim zakazane - je uz implementovane pre tab view pager
        nftViewpager.setAdapter(adapter);
        nftViewpager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        nftViewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                current_picked_index = position;
                resolveButtonsState(itemList.size(), current_picked_index);
                nftManagerViewModel.updateTexts(current_picked_index);  // premen novy nazov + popis

            }
        });
        nftViewpager.setVisibility(View.VISIBLE);
    }


    private void onNftsFetched(List<AddressNftsData> itemList){     //viewpager obrazky + data

        this.current_picked_index = 0;
        resolveButtonsState(itemList.size(), this.current_picked_index);

        if (itemList.isEmpty()){

            showNoNftsView();
        }
        else {

            showNftsView(itemList);
        }
    }

    private void showNftsView(List<AddressNftsData> itemList){

        nftManagerBinding.nftManNoNftsLabel.setVisibility(View.GONE);
        noNftImage.setVisibility(View.GONE);
        initNftViewPager(itemList);
        nftManagerViewModel.updateTexts(this.current_picked_index);
    }

    private void showNoNftsView(){
        Toast.makeText(getContext(), getString(R.string.nft_man_no_nfts_msg), Toast.LENGTH_LONG).show();
        noNftImage.setVisibility(View.VISIBLE);
        nftManagerBinding.nftManNoNftsLabel.setText(getString(R.string.nft_man_no_nfts_msg));
        nftManagerBinding.nftManNoNftsLabel.setVisibility(View.VISIBLE);
    }

    private void onNftsFetchFailed(NftFetchFailResponse response){

        Toast.makeText(getContext(),response.getMessage(), Toast.LENGTH_LONG).show();
        nftManagerBinding.nftManNoNftsLabel.setText(getString(R.string.nft_man_server_msg));
        noNftImage.setVisibility(View.VISIBLE);
        nftManagerBinding.nftManNoNftsLabel.setVisibility(View.VISIBLE);
        resolveButtonsState(0,0);
    }

    private void resolveButtonsState(int listSize, int currentPosition){

        if (listSize <1){

            //no arrows, pick btn inactive
            leftBtn.setVisibility(View.GONE);
            rightBtn.setVisibility(View.GONE);
            pickNftButton.setEnabled(false);
        }
        else if(listSize == 1){

            //1 prvok - pick btn active, no arrows
            leftBtn.setVisibility(View.GONE);
            rightBtn.setVisibility(View.GONE);
            pickNftButton.setEnabled(true);
        }
        else if((listSize > 1) && (currentPosition == 0)){

            // prvy prvok z n prvkov - len prava sipka
            leftBtn.setVisibility(View.GONE);
            rightBtn.setVisibility(View.VISIBLE);
            pickNftButton.setEnabled(true);
        }
        else if ((listSize > 1) && (currentPosition > 0) && (currentPosition < (listSize -1))){

            // prvok z otvoreneho intervalu (0,n)
            leftBtn.setVisibility(View.VISIBLE);
            rightBtn.setVisibility(View.VISIBLE);
            pickNftButton.setEnabled(true);
        }
        else if ((listSize > 1) && (currentPosition == (listSize-1))){

            //n ty posledny prvok z n prvkov
            leftBtn.setVisibility(View.VISIBLE);
            rightBtn.setVisibility(View.GONE);
            pickNftButton.setEnabled(true);
        }

    }


    private void setLoadingPage(){      //zatial nepouzivam

        AddressNftsData defaultData = new AddressNftsData();
        defaultData.setName("-");
        defaultData.setDescription("-");
        defaultData.setImageUrl(String.valueOf( R.drawable.custom_spinner));

        List<AddressNftsData> defaultList = new ArrayList<>();
        defaultList.add(defaultData);

        adapter.setItemList(defaultList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        int current_index = nftViewpager.getCurrentItem();  //aktualny index

        if (v.equals(nftManagerBinding.nftManLeftBtn)){

            // move index page --
            nftViewpager.setCurrentItem(current_index -1);

        }
        else if(v.equals(nftManagerBinding.nftManRightBtn)){

            // move index page ++
            nftViewpager.setCurrentItem(current_index +1);

        }
        else if(v.equals(nftManagerBinding.pickNftBtnBtn)){

            //start to organize - waypoint picker fragment TODO dorobit bundle so zvolenymi datami

            if (nftViewpager.getCurrentItem() != -1){

                try {

                    //presun sa na vyber waypointov + odovzdaj info o vybranom nft tokene
                    AddressNftsData nft = nftManagerViewModel.getNftsList().getValue().get(nftViewpager.getCurrentItem());
                    Bundle bundle = new Bundle();
                    bundle.putString(SERIALIZED_NFT, new Gson().toJson(nft));
                    Navigation.findNavController(v).navigate(R.id.action_wrappingFragment2_to_pickWaypointsFragment, bundle);

                }catch (NullPointerException e){

                    Log.e("Nft list get","Nullptr exception");
                }

            }





        }

    }
}