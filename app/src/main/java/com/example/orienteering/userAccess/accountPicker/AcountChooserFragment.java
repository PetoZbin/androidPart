package com.example.orienteering.userAccess.accountPicker;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentAcountChooserBinding;
import com.example.orienteering.databinding.FragmentLoginBinding;
import com.example.orienteering.dbWork.registration.UserCredentials;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.userAccess.accountAdding.responses.AddingSysResponse;
import com.example.orienteering.userAccess.accountPicker.pickerRecycler.PickerRecyclerAdapter;
import com.example.orienteering.userAccess.onboarding.LoginViewModel;
import com.example.orienteering.userAccess.onboarding.responses.LoginSuccessResponse;

import java.util.List;


public class AcountChooserFragment extends Fragment
        implements View.OnClickListener, PickerRecyclerAdapter.OnAccountPickedListener, PickerRecyclerAdapter.OnDeleteListener {

    public static final String FROM_LOGIN = "FROM_LOGIN"; // initiated from menu

    private AccountPickerViewModel pickerViewModel;
    private FragmentAcountChooserBinding pickerBinding;

    private PickerRecyclerAdapter pickerAdapter;

    private ImageButton backButton; //backbutton
    private ImageButton logoutButton; //logout
    private Button addAccountButton;

    private Boolean fromLogin = false;

    public AcountChooserFragment() {
        // Required empty public constructor
    }


    public static AcountChooserFragment newInstance(String param1, String param2) {
        AcountChooserFragment fragment = new AcountChooserFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//
//        }

       // Bundle bundle = getArguments();

        if (getArguments() != null){
            if (getArguments().containsKey(FROM_LOGIN)){

                fromLogin = getArguments().getBoolean(FROM_LOGIN);
                // pickerViewModel.checkAccountPicked();   // preskoc okno ak bol ucet navoleny
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        pickerBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_acount_chooser,container,false);
        pickerBinding.setLifecycleOwner(this);
        pickerViewModel = new ViewModelProvider(this).get(AccountPickerViewModel.class);
        pickerBinding.setPickerViewModel(pickerViewModel);

        //observuj, ci sa uz stiahli ucty z db

        pickerViewModel.getUserId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String userId) {
                //uzivatel si vybral ucet - prepni

                if (fromLogin){
                    pickerViewModel.checkAccountPicked();
                }
                else {

                    pickerViewModel.fetchAccounts();
                }
            }
        });

        setupRecycler();

        pickerViewModel.getUserAccounts().observe(getViewLifecycleOwner(), new Observer<List<UserCredentials>>() {
            @Override
            public void onChanged(List<UserCredentials> accounts) {
                //ucty stiahnute - treba vypisat v recycler view
                onAccountsFetched(accounts);
            }
        });

        pickerViewModel.getWasAccountPicked().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean wasPicked) {
                //uzivatel si vybral ucet - prepni
                if (wasPicked && (getView() != null)){

                    pickerViewModel.fetchAccounts();
                    Navigation.findNavController(getView()).navigate(R.id.action_acountChooserFragment_to_wrappingFragment2);
                }
                else {
                    // uzivatel si zatial nevybral - zobraz ucty
                    pickerViewModel.fetchAccounts();
                }
            }
        });

        pickerViewModel.getDelResponse().observe(getViewLifecycleOwner(), new Observer<AddingSysResponse>() {
            @Override
            public void onChanged(AddingSysResponse response) {
                // chyba pri zmazani - bud server alebo local db
                Toast.makeText(requireContext(),response.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        pickerViewModel.getLogoutEvent().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedOut) {
                // chyba pri zmazani - bud server alebo local db

                if (loggedOut && (getView() != null)){

                    Navigation.findNavController(getView()).navigate(R.id.action_acountChooserFragment_to_loginFragment3);
                }
            }
        });

        //check ci uz uzivatel predtym nevybral ucet

        addAccountButton = pickerBinding.addAccountBtn;
        addAccountButton.setOnClickListener(this);

        backButton = pickerBinding.pickerBackBtn;
        backButton.setOnClickListener(this);

        logoutButton = pickerBinding.pickerLogoutBtn;
        logoutButton.setOnClickListener(this);


        //nacitaj prihlaseneho uzivatela
        pickerViewModel.checkLogged(UserDatabase.getInstance(getContext()));

        return pickerBinding.getRoot();
    }

    private void setupRecycler(){

        RecyclerView recycler = pickerBinding.accPickerRecycler;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());  //posielam kontext
        pickerAdapter = new PickerRecyclerAdapter();    // itemlist - list poloziek
        pickerAdapter.setOnAccountPickedListener(this);
        pickerAdapter.setOnDeleteListener(this);
        //TODO listener na mazanie adapter.setOnDeleteListener(this);

        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(pickerAdapter);
    }


    @SuppressLint("NotifyDataSetChanged")   //iniciuje vykreslenie uctov
    private void onAccountsFetched(List<UserCredentials> accounts){

        if (accounts.isEmpty()){
            Toast.makeText(requireContext(),getString(R.string.acc_picker_no_accounts), Toast.LENGTH_LONG).show();
        }

            pickerAdapter.setItemList(accounts);
            pickerAdapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {

        if (v.equals(addAccountButton)){

            Navigation.findNavController(v).navigate(R.id.action_acountChooserFragment_to_accountAddingFragment);
        }
        else if (v.equals(logoutButton)){

            pickerViewModel.logout();
        }
        else if(v.equals(backButton)){

            Navigation.findNavController(v).navigate(R.id.action_acountChooserFragment_to_loginFragment3);
        }


    }


    //komunikacia s recycler view obsahom a viewmodelom

    @Override
    public void onDelete(int position) {
        pickerViewModel.deleteAccount(position);
    }

    @Override
    public void onAccountPicked(int position) {
        pickerViewModel.pickAccount(position);
    }
}