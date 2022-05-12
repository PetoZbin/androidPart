package com.example.orienteering.userAccess.onboarding.encKeySetup;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentAccountAddingBinding;
import com.example.orienteering.databinding.FragmentEncKeySetupBinding;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.userAccess.accountAdding.AccountAddingViewModel;


public class EncKeySetupFragment extends Fragment implements View.OnClickListener{

    private EncKeySetupViewmodel viewmodel;
    private FragmentEncKeySetupBinding binding;
    private Button encKeySubmitBtn;
    private ImageButton backBtn;

    public EncKeySetupFragment() {
        // Required empty public constructor
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

        binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_enc_key_setup,container,false);
        binding.setLifecycleOwner(this);
        viewmodel = new ViewModelProvider(this).get(EncKeySetupViewmodel.class);
        binding.setEncKeySetupViewModel(viewmodel);

        encKeySubmitBtn = binding.enckeySetupBtn;
        encKeySubmitBtn.setOnClickListener(this);

        backBtn = binding.encKeySetupBack;
        backBtn.setOnClickListener(this);

        viewmodel.getErrorResponse().observe(getViewLifecycleOwner(), new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse res) {

                if ((res != null) && (res.getMessage() != null)){
                    Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewmodel.getSuccessResponse().observe(getViewLifecycleOwner(), new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse res) {

                if ((res != null) && res.getSuccess()){
                    Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    moveBack();
                }
            }
        });


        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {

        if (v.equals(encKeySubmitBtn)){

            viewmodel.persistEncKey();
        }
        else if (v.equals(backBtn)){

           moveBack();

        }

    }

    private void moveBack(){

        if (getActivity() != null){

            getActivity().onBackPressed();
        }
    }

}