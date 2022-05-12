package com.example.orienteering.nfts.nftToCompetition.compSummary.summaryDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentCompApproveDialogBinding;
import com.example.orienteering.nfts.nftToCompetition.compSummary.CompSummaryViewModel;
import com.example.orienteering.userAccess.onboarding.Validator;

import java.math.BigDecimal;
import java.math.BigInteger;


public class CompApproveDialogFragment extends AppCompatDialogFragment implements View.OnClickListener {


    private FragmentCompApproveDialogBinding approveDialogBinding;
    private CompSummaryDialogViewModel approveViewModel;

    private EditText passwordEditText;
    private ImageView spinner;
    private ImageButton yesBtn;
    private ImageButton noBtn;
    private View spinnerLayout;
    private View contentLayout;

    private Dialog dialog;
    private OnConsentListener consentListener;


//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//        approveDialogBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_comp_approve_dialog,container,false);
//        summaryBinding.setLifecycleOwner(this);
//        summaryViewModel = new ViewModelProvider(this).get(CompSummaryViewModel.class);
//        summaryBinding.setComSummaryViewModel(summaryViewModel);
//
//
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        //LayoutInflater inflater = getActivity().getLayoutInflater();
        //View view = inflater.inflate(R.layout.fragment_comp_approve_dialog, null);

        approveDialogBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_comp_approve_dialog,null,false);
        approveDialogBinding.setLifecycleOwner(this);
        approveViewModel = new ViewModelProvider(this).get(CompSummaryDialogViewModel.class);
        approveDialogBinding.setCompApproveViewModel(approveViewModel);

        dialogBuilder.setView(approveDialogBinding.getRoot());
        dialog = dialogBuilder.create();

        //spinner = approveDialogBinding.wayApproveSpinner;// view.findViewById(R.id.way_approve_spinner);
        passwordEditText = approveDialogBinding.wayApprovalPassword; //view.findViewById(R.id.way_approval_password);
        yesBtn = approveDialogBinding.wayApprovalYesBtn;// view.findViewById(R.id.way_approval_yes_btn);
        noBtn = approveDialogBinding.wayApprovalNoBtn; // view.findViewById(R.id.way_approval_no_btn);

        spinnerLayout = approveDialogBinding.wayApproveSpinnerLayout;
        contentLayout = approveDialogBinding.wayApproveContentLayout;

        spinnerLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);

        yesBtn.setOnClickListener(this);
        noBtn.setOnClickListener(this);

        //spinner.setVisibility(View.VISIBLE);
//
//        dialogBuilder.setView(view);
//        dialog = dialogBuilder.create();
//
//
        approveViewModel.getPickedAddress().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String address) {

                if (Validator.isAddressValid(address)){

                    approveViewModel.fetchTransactionInfo(address);
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.val_invalid_address), Toast.LENGTH_LONG).show();

                }
            }
        });

        //check zadania spravneho hesla
        approveViewModel.getPasswordOk().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean passwordOk) {

                if (passwordOk){
                    //heslo ok - spusti loading - spusti prevod pomocou onconsent
                    contentLayout.setVisibility(View.GONE);
                    spinnerLayout.setVisibility(View.VISIBLE);
                    consentListener.onConsent(approveViewModel.getPassword().getValue(), approveViewModel.getTippedFee());
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.val_incorrect_enc_key), Toast.LENGTH_LONG).show();
                    spinnerLayout.setVisibility(View.GONE);
                    contentLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        approveViewModel.getEnoughBalance().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean enoughBalance) {

               // spinnerLayout.setVisibility(View.GONE);
              //  contentLayout.setVisibility(View.VISIBLE);

                if (!enoughBalance){
                    //slaby zostatok voci poplatku
                    yesBtn.setEnabled(false);
                    yesBtn.setImageAlpha(0x3F);
                    Toast.makeText(getContext(), getString(R.string.way_summary_balance_error_funds), Toast.LENGTH_LONG).show();
                }
                else {
                    yesBtn.setEnabled(true);
                    yesBtn.setImageAlpha(0xFF);
                }
            }
        });

        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface OnConsentListener{

        //zavolaj funckiu parrent fragmentu skus overit heslo a vykonat transakciu, ak ok - zavri dialog
        // tiez sa presun na home wrapping fragment
        // ak NOK  - ostan, toast s chybou
        void onConsent(String password, BigInteger tippedFee);  // ziskal som aktualny poplatok - priratam 15% tip - uistenim sa ze vytazia
    }

    public void setConsentListener(OnConsentListener consentListener) {
        this.consentListener = consentListener;
    }

    @Override
    public void onClick(View v) {

        if (v.equals(yesBtn)){

            String password = passwordEditText.getText().toString();

            if(Validator.isPasswordValid(password)){   // heslo validne?

                contentLayout.setVisibility(View.GONE);
                spinnerLayout.setVisibility(View.VISIBLE);
                approveViewModel.checkPassword();
            }
            else {
                spinnerLayout.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), getString(R.string.login_invalid_password), Toast.LENGTH_LONG).show();
            }


        }
        else if (v.equals(noBtn)){ dialog.cancel(); }

    }


    private void checkPasswordInDb(){



    }

    public void initiateTransactionError(String errorMessage){

        spinnerLayout.setVisibility(View.INVISIBLE);
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

    }

    public void resetDialog(){

        approveViewModel.insertedPassword.postValue("");
        yesBtn.setEnabled(false);
        yesBtn.setImageAlpha(0x3F);

        spinnerLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void onResume() {
        super.onResume();

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.75);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.7);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corners);
    }
}