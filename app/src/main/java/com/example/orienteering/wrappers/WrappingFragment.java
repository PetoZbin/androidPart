package com.example.orienteering.wrappers;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentWrappingBinding;
import com.example.orienteering.userAccess.onboarding.RegistrationViewModel;
import com.example.orienteering.userAccess.onboarding.responses.RegSuccessResponse;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;


public class WrappingFragment extends Fragment
        implements TabLayout.OnTabSelectedListener, View.OnClickListener, PopupMenu.OnMenuItemClickListener{


    private ViewPager2 viewPager;
    private WrappingViewAdapter viewAdapter;

    private androidx.appcompat.widget.Toolbar menuToolBar;    //TODO - zatial neexistujuca horna toolbar
    private androidx.appcompat.widget.Toolbar pageToolBar;    //ukazuje, ktora stranka je aktualne zobrazena
    private TabLayout tabLayout;
    private FragmentWrappingBinding wrappingBinding;
    private WrappingViewModel wrappingViewModel;

    private ImageButton refreshBtn;
    private ImageButton menuBtn;
    private TextView heading;

    private String [] headingsArray;

    public WrappingFragment() {
        // Required empty public constructor
    }


    public static WrappingFragment newInstance(String param1, String param2) {
        WrappingFragment fragment = new WrappingFragment();
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

        wrappingBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_wrapping,container,false);
        wrappingBinding.setLifecycleOwner(this);
        wrappingViewModel = new ViewModelProvider(this).get(WrappingViewModel.class);
        wrappingBinding.setWrappingViewmodel(wrappingViewModel);

        heading = wrappingBinding.wrappingHeading;
        menuBtn = wrappingBinding.wrappingMenu;
        menuBtn.setOnClickListener(this);
        refreshBtn = wrappingBinding.wrappingRefreshBtn;

        addPageToolbar();
        initViewPager();

        //napln pole stringov
        headingsArray = new String[]{getString(R.string.nft_man_heading),
                getString(R.string.comp_map_heading), getString(R.string.standings_heading)};

        // Inflate the layout for this fragment
        return wrappingBinding.getRoot();
    }


    private void addPageToolbar(){
        pageToolBar = wrappingBinding.pageToolbar;
        tabLayout = wrappingBinding.menuBarTabLayout;
        tabLayout.addOnTabSelectedListener(this);

        try {

            tabLayout.getTabAt(0).getIcon().setColorFilter(getContext().getColor(R.color.dark_purple), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(1).getIcon().setColorFilter(getContext().getColor(R.color.gray), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(3).getIcon().setColorFilter(getContext().getColor(R.color.gray), PorterDuff.Mode.SRC_IN);

        }catch (NullPointerException e){
            Log.e("Tabs in wrapping fragment","Nullptr exception");
        }

    }


    private void initViewPager(){

        viewAdapter = new WrappingViewAdapter(this);

        viewPager = wrappingBinding.wrappingPager;
        viewPager.setAdapter(viewAdapter);
        viewPager.setUserInputEnabled(false);   //swiping nepodporujem - potrebujem siping po mape

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {

                heading.setText(headingsArray[position]);
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        try {

            tab.getIcon().setColorFilter(getContext().getColor(R.color.dark_purple), PorterDuff.Mode.SRC_IN);
        }catch (NullPointerException ex){
            Log.e("Tabs in wrapping fragment","Nullptr exception");
        }

        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

        try {

            tab.getIcon().setColorFilter(getContext().getColor(R.color.gray), PorterDuff.Mode.SRC_IN);
        }catch (NullPointerException ex){
            Log.e("Tabs in wrapping fragment","Nullptr exception");
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onClick(View v) {

        if (v.equals(wrappingBinding.wrappingMenu)  && (getContext() !=null)){    //klikol na menu

            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.setOnMenuItemClickListener(this);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.wraping_menu, popupMenu.getMenu());
            popupMenu.show();
        }

    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_item_chooseAccount:

                if(getView() != null){

                    Navigation.findNavController(getView()).navigate(R.id.action_wrappingFragment2_to_acountChooserFragment);
                }

                return true;

            case R.id.menu_item_logout:

                if (getView() != null){

                    //observuj uspesne odhlasenie

                    wrappingViewModel.getIsLoggedOut().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean loggedOut) {
                            //po uspesnom prihlaseni pride response, ktoru observujem
                            if (loggedOut){
                                // v databaze odhlaseny - presun do loginu
                                Navigation.findNavController(getView()).navigate(R.id.action_wrappingFragment2_to_loginFragment3);
                            }
                        }
                    });


                    // odhlas v databaze
                    wrappingViewModel.logOut();

                }

            default:
                return  false;
        }

    }

}