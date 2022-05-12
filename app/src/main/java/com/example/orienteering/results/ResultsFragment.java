package com.example.orienteering.results;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentResultsBinding;
import com.example.orienteering.results.mainListView.ResultsListViewAdapter;
import com.example.orienteering.retrofit.patternClasses.BasicResultPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;

import java.security.PrivateKey;
import java.util.List;


public class ResultsFragment extends Fragment implements View.OnClickListener{

    public static final int LOW_ALPHA = 0x3F;
    public static final int FULL_ALPHA = 0xFF;
    public static final int MAX_PAGE_ITEMS = 8;
    public static final String COMPETITION_ID = "competitionId";

    private FragmentResultsBinding binding;
    private ResultsFragmentViewModel viewmodel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_results,container,false);
        binding.setLifecycleOwner(this);
        viewmodel = new ViewModelProvider(this).get(ResultsFragmentViewModel.class);
        binding.setResultsViewModel(viewmodel);
        hideArrows();
        switchArrowsActive(false);

        viewmodel.getUserId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String userId) {
                if (userId != null){
                    viewmodel.initResults(MAX_PAGE_ITEMS);    //po nacitani userId z DB natiahni sutaze zo server DB
                }
            }
        });

        viewmodel.getErrorResponse().observe(getViewLifecycleOwner(), new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse res) {
                if (res != null){
                    Toast.makeText(getContext(),res.getMessage(),Toast.LENGTH_SHORT).show();
                }
                switchArrowsActive(true);
            }
        });

        viewmodel.getPageResults().observe(getViewLifecycleOwner(), new Observer<List<BasicResultPattern>>() {
            @Override
            public void onChanged(List<BasicResultPattern> results) {
                if ((results != null) && !results.isEmpty()){
                    switchArrowsActive(true);
                    resolveArrows();
                    setPageText();
                    initListView(results);
                }
            }
        });

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void initListView(List<BasicResultPattern> results){

        try {

            ResultsListViewAdapter adapter = new ResultsListViewAdapter(getContext(),results);
            binding.fragResultsListview.setAdapter(adapter);
            binding.fragResultsListview.setClickable(true);
            binding.fragResultsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // presun sa na fragment result detail, posli tam objekt basic result

                    String competitionId = viewmodel.getCompetitionIdByListPos(position);

                    if (competitionId != null){
                        //naslo sutaz na danej pozicii - prepni fragment

                        Bundle bundle = new Bundle();
                        bundle.putString(COMPETITION_ID, competitionId);
                        Navigation.findNavController(view).navigate(R.id.action_wrappingFragment2_to_resultDetailFragment, bundle);

                    }
                    else {
                        Toast.makeText(getContext(),getString(R.string.results_competition_found_error),Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }catch (NullPointerException ex){

            Log.e("Init result listview:", "Nullptr exception");
        }
    }

    private void resolveArrows(){
        //sipky na zmenu stranok

        try{

            int currentPage = viewmodel.getCurrentPage().getValue();
            int lastPage = viewmodel.getCurrentPage().getValue();


            if ((currentPage == 1) && (currentPage < lastPage)){    //prva z n stranok

                binding.fragResultsLeftBtn.setVisibility(View.INVISIBLE);
                binding.fragResultsRightBtn.setVisibility(View.VISIBLE);
            }
            else if((currentPage == lastPage) && (currentPage == 1)){   //len jedna stranka

                binding.fragResultsLeftBtn.setVisibility(View.INVISIBLE);
                binding.fragResultsRightBtn.setVisibility(View.INVISIBLE);
            }
            else if((currentPage == lastPage) && (lastPage > 1)){    //n ta stranka z n stranok

                binding.fragResultsLeftBtn.setVisibility(View.VISIBLE);
                binding.fragResultsRightBtn.setVisibility(View.INVISIBLE);
            }
            else if((currentPage < lastPage) && (currentPage > 1)){ // stranka v otv. intervale (1,n)

                binding.fragResultsLeftBtn.setVisibility(View.VISIBLE);
                binding.fragResultsRightBtn.setVisibility(View.VISIBLE);
            }
            else {
                binding.fragResultsLeftBtn.setVisibility(View.INVISIBLE);
                binding.fragResultsRightBtn.setVisibility(View.INVISIBLE);
            }

        }catch (NullPointerException ex){
            Log.e("Page variables:", "Nullptr exception");
            binding.fragResultsLeftBtn.setVisibility(View.INVISIBLE);
            binding.fragResultsRightBtn.setVisibility(View.INVISIBLE);
        }



    }

    private void switchArrowsActive(Boolean activate){

        if (activate){

            binding.fragResultsLeftBtn.getBackground().setAlpha(FULL_ALPHA);
            binding.fragResultsLeftBtn.setEnabled(true);
            binding.fragResultsRightBtn.getBackground().setAlpha(FULL_ALPHA);
            binding.fragResultsRightBtn.setEnabled(true);

        }else {

            binding.fragResultsLeftBtn.getBackground().setAlpha(LOW_ALPHA);
            binding.fragResultsLeftBtn.setEnabled(false);
            binding.fragResultsRightBtn.getBackground().setAlpha(LOW_ALPHA);
            binding.fragResultsRightBtn.setEnabled(false);
        }
    }

    private void hideArrows(){

        binding.fragResultsLeftBtn.setVisibility(View.INVISIBLE);
        binding.fragResultsRightBtn.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void setPageText(){ //aktualizuje text: cislo strany/mnozstvo stran

        if ((viewmodel.getCurrentPage()) != null && (viewmodel.getLastPage()!=null)){

            binding.fragResultsPageTextview.setText(""+viewmodel.getCurrentPage()+"/"+viewmodel.getLastPage());
        }
    }


    @Override
    public void onClick(View v) {

        if (v.equals(binding.fragResultsRightBtn)){

            try {
                if (viewmodel.getCurrentPage().getValue().intValue() < viewmodel.getLastPage().getValue().intValue()){

                    switchArrowsActive(false);
                    viewmodel.fetchResults(viewmodel.getUserId().getValue(),
                            viewmodel.getCurrentPage().getValue().intValue() +1, MAX_PAGE_ITEMS);
                }

            }catch (NullPointerException e){
                switchArrowsActive(true);
            }
        }
        else if (v.equals(binding.fragResultsLeftBtn)){

            try {
                if (viewmodel.getCurrentPage().getValue().intValue() > 1){

                    switchArrowsActive(false);
                    viewmodel.fetchResults(viewmodel.getUserId().getValue(),
                            viewmodel.getCurrentPage().getValue().intValue() -1, MAX_PAGE_ITEMS);
                }

            }catch (NullPointerException e){
                switchArrowsActive(true);
            }
        }

    }
}