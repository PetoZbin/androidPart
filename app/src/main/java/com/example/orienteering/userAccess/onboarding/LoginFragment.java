package com.example.orienteering.userAccess.onboarding;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentLoginBinding;
import com.example.orienteering.notification.NotifWorker;
import com.example.orienteering.retrofit.responseClasses.login.LoginResponse;
import com.example.orienteering.userAccess.onboarding.responses.LoginFailResponse;
import com.example.orienteering.userAccess.onboarding.responses.LoginSuccessResponse;
import com.example.orienteering.userAccess.onboarding.responses.RegFailResponse;

import java.util.concurrent.TimeUnit;


public class LoginFragment extends Fragment implements View.OnClickListener {

    public static final String USERID = "USERID";
    public static final String USERNAME = "USERNAME";
    public static final String FROM_LOGIN = "FROM_LOGIN"; // initiated from menu

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding loginBinding;
    private Button loginButton;
    private TextView registrationText;



    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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

        loginBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_login,container,false);
        loginBinding.setLifecycleOwner(this);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginBinding.setLoginViewModel(loginViewModel);

        loginButton = loginBinding.loginBtn; //fire registration process
        loginButton.setOnClickListener(this);

        registrationText = loginBinding.regLoginClickable;   // redirection to register fragment
        registrationText.setOnClickListener(this);


        Bundle bundle = getArguments();

        if (getArguments() != null){

            if (getArguments().containsKey(USERNAME)){
                prefillUsername(bundle.getString(USERNAME));
            }
        }

        loginViewModel.getLoginSucessResponse().observe(getViewLifecycleOwner(), new Observer<LoginSuccessResponse>() {
            @Override
            public void onChanged(LoginSuccessResponse response) {
                //po uspesnom prihlaseni pride response, ktoru observujem
                onLoginSucess(response);
            }
        });

        loginViewModel.getIsOnlyLogged().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean logged) {
                //po uspesnom prihlaseni pride response, ktoru observujem
                if (logged){

                    LoginSuccessResponse res = new LoginSuccessResponse();
                    res.setMessage("Logged user found");
                    onLoginSucess(res);
                }

            }
        });

        loginViewModel.getHasLoggedAndPicked().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedandPicked) {
                //po uspesnom prihlaseni pride response, ktoru observujem
                if (loggedandPicked){
                    onAccountPickedAndLogged();
                }

            }
        });


        loginViewModel.getLoginFailResponse().observe(getViewLifecycleOwner(), new Observer<LoginFailResponse>() {
            @Override
            public void onChanged(LoginFailResponse failResponse) {

                onLoginFailed(failResponse);
            }
        });

        loginViewModel.checkUserWithAccountPicked();

        return loginBinding.getRoot();
    }


    private void prefillUsername(String username){ // ak sa uzivatel predtym prihlasil - predvypln login z registracie

        loginViewModel.getUserName().postValue(username);

    }

    private void onLoginSucess(LoginSuccessResponse response){

        //prechod na vyber uctu
        Toast.makeText(requireContext(),response.getMessage(), Toast.LENGTH_LONG).show();

        if (getView() != null){

            setupNotifPeriodicFetch();

            Bundle bundle = new Bundle();
            bundle.putBoolean(FROM_LOGIN, true);
            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment3_to_acountChooserFragment, bundle);
        }
        else Log.e("onloginSuccess error:", "getView is null");

    }


    private void onAccountPickedAndLogged(){

        if (getView() != null)

            setupNotifPeriodicFetch();

            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment3_to_wrappingFragment2, null);
    }

    private void onLoginFailed(LoginFailResponse failResponse){

        //TODO resolve fail response
        Toast.makeText(requireContext(),failResponse.getMessage(), Toast.LENGTH_LONG).show();
    }


    // najmensia perioda je 15 minut
    //na zaklade naucneho videa: https://www.youtube.com/watch?v=EmdoVqSdTHs
    private void setupNotifPeriodicFetch(){

        if (getContext() != null){

            PeriodicWorkRequest workReq = new PeriodicWorkRequest.Builder(NotifWorker.class, 15, TimeUnit.MINUTES)
                    .addTag("OrienteeringNotifRequest")
                    .build();

            WorkManager workManager = WorkManager.getInstance(getContext());
            workManager.enqueue(workReq);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.equals(loginBinding.loginBtn)){

            //login procedure
        }
        else if(v.equals(loginBinding.regLoginClickable)){

            //move to register
            Navigation.findNavController(v).navigate(R.id.action_loginFragment3_to_registerFragment5);
        }

    }
}