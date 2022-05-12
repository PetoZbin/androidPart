package com.example.orienteering.userAccess.accountAdding;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentAccountAddingBinding;
import com.example.orienteering.databinding.FragmentLoginBinding;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.userAccess.accountAdding.responses.AddingSysResponse;
import com.example.orienteering.userAccess.onboarding.LoginViewModel;
import com.example.orienteering.userAccess.onboarding.responses.LoginSuccessResponse;


public class AccountAddingFragment extends Fragment implements View.OnClickListener{


    private AccountAddingViewModel addingViewModel;
    private FragmentAccountAddingBinding addingBinding;
    private Button addingButton;
    private ImageButton accAddingBackBtn;
    private ImageButton accAddingLockBtn;
    private EditText privKeyField;
    private TextView addressTextView;


    public AccountAddingFragment() {
        // Required empty public constructor
    }


    public static AccountAddingFragment newInstance(String param1, String param2) {
        AccountAddingFragment fragment = new AccountAddingFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {


        addingBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_account_adding,container,false);
        addingBinding.setLifecycleOwner(this);
        addingViewModel = new ViewModelProvider(this).get(AccountAddingViewModel.class);
        addingBinding.setAddingViewModel(addingViewModel);

        addingButton = addingBinding.addAccountBtn; //fire registration process
        addingButton.setOnClickListener(this);

        accAddingBackBtn = addingBinding.accAddingBack;
        accAddingBackBtn.setOnClickListener(this);

        accAddingLockBtn = addingBinding.accAddingLockBtn;
        accAddingLockBtn.setOnClickListener(this);

        privKeyField = addingBinding.accAddPrivEdittext;

        privKeyField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(!v.hasFocus()){  //ak private key stratil focus - vyrataj z neho adresu a public key
                    addingViewModel.updateCredentials();
                }
            }
        });

        addingViewModel.getAddingResponse().observe(getViewLifecycleOwner(), new Observer<AddingSysResponse>() {
            @Override
            public void onChanged(AddingSysResponse response) {
                // ci je uspesna / neuspesna - podla toho dalsi postup
                resolveResponse(response);
            }
        });

        addingViewModel.getErrorResponse().observe(getViewLifecycleOwner(), new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse res) {

                if ((res != null) && (res.getMessage() != null)){
                    Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return addingBinding.getRoot();
    }

    @Override
    public void onClick(View v) {

        if (v.equals(addingButton)){

            //adding fired from xml
        }
        else if(v.equals(addingBinding.accAddingLockBtn)){
            //move to login - logout clicked
            Navigation.findNavController(v).navigate(R.id.action_accountAddingFragment_to_encKeySetupFragment);
        }
        else if(v.equals(accAddingBackBtn)){

            Navigation.findNavController(v).navigate(R.id.action_accountAddingFragment_to_acountChooserFragment);
        }
    }

    private void resolveResponse(AddingSysResponse response){

        if (response.correctlyAdded()){ // uspesne pridany ucet

            onAddingSuccess(response);
            //
        }
        else onAddingFailed(response);
    }

    private void onAddingSuccess(AddingSysResponse response){

        Toast.makeText(requireContext(),response.getMessage(), Toast.LENGTH_LONG).show();

        // prepni späť na fragment accounts TODO
        try {
            Navigation.findNavController(getView()).navigate(R.id.action_accountAddingFragment_to_acountChooserFragment);
        }catch (NullPointerException e){
            Log.e("getView() in adding", "get view null");
        }

    }

    private void onAddingFailed(AddingSysResponse response){

        if (response.getPivateKeyInvalid()){

            // TODO set error priv key field
            Toast.makeText(requireContext(),response.getKeyMessage(), Toast.LENGTH_LONG).show();
        }
        else if(response.getServerError()){

            Toast.makeText(requireContext(),response.getServerMessage(), Toast.LENGTH_LONG).show();
        }
        else if (response.getDbError()){
            // TODO private key is used by another user
            Toast.makeText(requireContext(),response.getDbMessage(), Toast.LENGTH_LONG).show();
        }
        else {

            Toast.makeText(requireContext(),response.getMessage(), Toast.LENGTH_LONG).show();
        }
    }




}