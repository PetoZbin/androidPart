package com.example.orienteering.userAccess.onboarding;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentRegisterBinding;
import com.example.orienteering.userAccess.onboarding.responses.RegFailResponse;
import com.example.orienteering.userAccess.onboarding.responses.RegSuccessResponse;

import java.time.Duration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener{

    public static final String USERID = "USERID";
    public static final String USERNAME = "USERNAME";
    private RegistrationViewModel regViewModel;
    private FragmentRegisterBinding regBinding;
    private Button regButton;
    private TextView loginText;
    private EditText privKeyField;


    public RegisterFragment() {
        // Required empty public constructor
    }


    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        regBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_register,container,false);
        regBinding.setLifecycleOwner(this);
        regViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
        regBinding.setRegistrationViewModel(regViewModel);

        regViewModel.getSuccessResponse().observe(getViewLifecycleOwner(), new Observer<RegSuccessResponse>() {
            @Override
            public void onChanged(RegSuccessResponse regSuccessResponse) {
                //po uspesnom prihlaseni pride response, ktoru observujem
                onRegSuccess(regSuccessResponse);
            }
        });

        regViewModel.getFailResponse().observe(getViewLifecycleOwner(), new Observer<RegFailResponse>() {
            @Override
            public void onChanged(RegFailResponse regFailResponse) {
                //po uspesnom prihlaseni pride response, ktoru observujem
                onRegFailed(regFailResponse);
            }
        });

        regButton = regBinding.registerBtn; //fire registration process
        regButton.setOnClickListener(this);

        loginText = regBinding.regLoginClickable;   // redirection to register fragment
        loginText.setOnClickListener(this);

        privKeyField = regBinding.regPrivKeyInput;
        privKeyField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(!v.hasFocus()){  //ak private key stratil focus - vyrataj z neho adresu a public key
                    regViewModel.updateCredentials();
                }
            }
        });




        return regBinding.getRoot();

    }


    private void onRegSuccess(RegSuccessResponse response){         //obsluha po uspesnej registracii


        Toast.makeText(requireContext(),R.string.reg_success_msg, Toast.LENGTH_LONG).show();    //uspesne sa registroval

        try {
            //posli username

            Bundle bundle = new Bundle();
            bundle.putString(USERNAME, response.getUserName());
            bundle.putString(USERID, response.getUserId());

            // presun sa na login obrazovku, predvypln meno
            Navigation.findNavController(getView()).navigate(R.id.action_registerFragment5_to_loginFragment3, bundle);
        }catch (NullPointerException e){

            Log.e("reg nav controller", "view from getView is null");
        }

    }

    private void onRegFailed(RegFailResponse response){         //obsluha po uspesnej registracii


        Toast.makeText(requireContext(),response.getMessage(), Toast.LENGTH_LONG).show();    //registracia neuspesna


    }


    @Override
    public void onClick(View v) {

        if (v.equals(regButton)){

            //register
        }
        else if (v.equals(loginText)){

            //redirect to login
            Navigation.findNavController(v).navigate(R.id.action_registerFragment5_to_loginFragment3);

        }

    }

}