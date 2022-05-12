package com.example.orienteering.nfts.nftToCompetition.compSummary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentCompSummaryBinding;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.nfts.nftToCompetition.compSummary.summaryDialog.CompApproveDialogFragment;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddData;
import com.example.orienteering.userAccess.onboarding.Validator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CompSummaryFragment extends Fragment
        implements View.OnClickListener, View.OnFocusChangeListener, CompApproveDialogFragment.OnConsentListener {

    public static final String SERIALIZED_WAYPOINTS = "serializedWaypoints";
    public static final String SERIALIZED_NFT = "serializedNft";

    Calendar calendar;
    SimpleDateFormat dateFormat;

    private FragmentCompSummaryBinding summaryBinding;
    private CompSummaryViewModel summaryViewModel;
    private EditText compDateTimeField;
    private ImageButton nextBtn;
    private ImageButton backBtn;

    private CompApproveDialogFragment dialogInstance;


    public CompSummaryFragment() {
        // Required empty public constructor
    }


    public static CompSummaryFragment newInstance(String param1, String param2) {
        CompSummaryFragment fragment = new CompSummaryFragment();
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
        // Inflate the layout for this fragment

        summaryBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_comp_summary,container,false);
        summaryBinding.setLifecycleOwner(this);
        summaryViewModel = new ViewModelProvider(this).get(CompSummaryViewModel.class);
        summaryBinding.setComSummaryViewModel(summaryViewModel);

        nextBtn = summaryBinding.wayPickerContinue;
        nextBtn.setOnClickListener(this);
        nextBtn.setEnabled(false);
        nextBtn.setImageAlpha(0x3F);

        compDateTimeField =summaryBinding.wayCompDatetime;
        compDateTimeField.setOnClickListener(this);
        compDateTimeField.setOnFocusChangeListener(this);

       // SimpleDateFormat sqlFormat = new SimpleDateFormat( "yyyy-MM-dd kk:mm", Locale.getDefault());    //pre sekundy :ss, je to sql format
        initDatePicking();


        backBtn = summaryBinding.wayPickerBackBtn;
        backBtn.setOnClickListener(this);

        if ((getArguments() != null)
                && (getArguments().containsKey(SERIALIZED_WAYPOINTS)) && (getArguments().containsKey(SERIALIZED_NFT))){
                summaryViewModel.setWaypoints(getArguments().getString(SERIALIZED_WAYPOINTS));
                summaryViewModel.setChosenNft(getArguments().getString(SERIALIZED_NFT));
        }

        summaryViewModel.getWaypoints().observe(getViewLifecycleOwner(), new Observer<List<CustomWaypointDesc>>() {
            @Override
            public void onChanged(List<CustomWaypointDesc> items) {
                //po uspesnom prihlaseni pride response, ktoru observujem
                initRecycler(items);
            }
        });

        summaryViewModel.getMaxCompetitors().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String competitors) {

                if (Validator.isNumCompetitorsValid(competitors) && (summaryViewModel.getUserId().getValue() != null)){
                    //aspon dvaja sutaziaci
                    nextBtn.setEnabled(true);
                    nextBtn.setImageAlpha(0xFF);
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.way_summary_num_comp_error), Toast.LENGTH_LONG).show();
                    nextBtn.setEnabled(false);
                    nextBtn.setImageAlpha(0x3F);
                }
            }
        });

        summaryViewModel.getCompetitionAddResponse().observe(getViewLifecycleOwner(), new Observer<CompetitionAddData>() {
            @Override
            public void onChanged(CompetitionAddData response) {
                //po uspesnom prihlaseni pride response, ktoru observujem
                if (response.getSuccess()){
                    onCompetitionRegistered(response);
                }
                else {
                    onCompetitionError(response.getMessage());
                }
            }
        });

        return summaryBinding.getRoot();
    }

    private void initRecycler(List<CustomWaypointDesc> items){

        RecyclerView recycler = summaryBinding.wayCompRecycler;
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        CompSummaryRecyclerAdapter adapter = new CompSummaryRecyclerAdapter();
        adapter.setItemList(items);

        recycler.setAdapter(adapter);
    }

    private void initDatePicking(){

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
    }




    private  TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE,minute);
            calendar.set(Calendar.SECOND, 0);
            summaryViewModel.updateDate(dateFormat.format(calendar.getTime()));
        }
    };

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),true).show();
        }
    };

    private void showDateTimepicker(){

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        DatePickerDialog dateDialog =  new DatePickerDialog(getContext(), R.style.Theme_AppCompat_Light_Dialog, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        dateDialog.show();

    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        if (v.equals(summaryBinding.wayCompDatetime) && hasFocus){

            showDateTimepicker();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.equals(nextBtn)){

            // otvor fragment approval

            dialogInstance = new CompApproveDialogFragment();
            dialogInstance.setTargetFragment(this, 0);
            dialogInstance.setConsentListener(this);
            dialogInstance.show(getFragmentManager().beginTransaction(), "wayopt");
        }
        else if(v.equals(backBtn)){

            // krok spat
            getActivity().onBackPressed();
        }
        else if (v.equals(summaryBinding.wayCompDatetime)){

            showDateTimepicker();
        }
    }

    @Override
    public void onConsent(String password, BigInteger tippedFee) {

        //sem sa dostane po potvrdeni a kontrole hesla, tiez kontrole dostatku minci na vykonanie transackie (voci aktualnej cene)
        //zacni prevod
        summaryViewModel.transferNft(password, tippedFee);


      //  summaryViewModel.checkPassword(password);
    }

    private void onCompetitionRegistered(CompetitionAddData competition){

        //toast message
        //naviguj na wrapping fragment
        //zavri dialog
        try {
            dialogInstance.getDialog().cancel();
        }catch (NullPointerException ex){
            Log.e("Null ptr exception:","dialog.cancel() from onCompetitionRegistered");
        }

        Toast.makeText(getContext(),competition.getMessage(), Toast.LENGTH_SHORT).show();

        if (getView() != null){
            Navigation.findNavController(getView()).navigate(R.id.action_compSummaryFragment_to_wrappingFragment2);
        }
    }

    private void onCompetitionError(String message){

        try {
            dialogInstance.initiateTransactionError(message);
            dialogInstance.resetDialog();

        }catch (NullPointerException ex){
            Log.e("Null ptr exception:","dialog.cancel() from onCompetitionRegistered");
        }


    }


}