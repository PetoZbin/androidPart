package com.example.orienteering.nfts.nftToCompetition.compSummary.summaryDialog;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.crypto.Web3JClient;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UsersDao;
import com.example.orienteering.helpers.CipherHelper;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompSummaryDialogViewModel extends LoggedViewmodel {

    BigDecimal TIP_CONST = new BigDecimal("1.401");   //nasobok gas price - platim viac ako posledna cena paliva

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    MutableLiveData<String> insertedPassword = new MutableLiveData<String>();
    private MutableLiveData<Boolean> passwordOk = new MutableLiveData<Boolean>();

    private MutableLiveData<String> currentGasPrice = new MutableLiveData<String>("fetching");
    private MutableLiveData<String> currentBalance = new MutableLiveData<String>("fetching");
    private MutableLiveData<Boolean> enoughBalance = new MutableLiveData<Boolean>();

    private BigInteger tippedFee = new BigInteger("0");

    private final UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();

    public MutableLiveData<String> getPassword() {
        return insertedPassword;
    }

    public void setPassword(MutableLiveData<String> password) {
        this.insertedPassword = password;
    }

    public MutableLiveData<Boolean> getPasswordOk() {
        return passwordOk;
    }

    public void setPasswordOk(MutableLiveData<Boolean> passwordOk) {
        this.passwordOk = passwordOk;
    }

    public void setEnoughBalance(MutableLiveData<Boolean> enoughBalance) {
        this.enoughBalance = enoughBalance;
    }

    public MutableLiveData<Boolean> getEnoughBalance() {
        return this.enoughBalance;
    }

    public MutableLiveData<String> getCurrentGasPrice() {
        return currentGasPrice;
    }

    public void setCurrentGasPrice(MutableLiveData<String> currentGasPrice) {
        this.currentGasPrice = currentGasPrice;
    }

    public MutableLiveData<String> getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(MutableLiveData<String> currentBalance) {
        this.currentBalance = currentBalance;
    }

    public CompSummaryDialogViewModel(@NonNull Application application) {
        super(application);
        super.checkLogged(UserDatabase.getInstance(getApplication()));
    }

    public BigInteger getTippedFee() {
        return tippedFee;
    }

    public void checkPassword(){

        //check password if correct - bezi na background vlakne - observujem

        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                () -> {

                    UserRegistration user = usersDao.getUserById(getUserId().getValue());

                    String hashedEncryptionKey = user.getEncryptionKey();


                    try {

                        if (CipherHelper.verifyPassword(insertedPassword.getValue(),hashedEncryptionKey)){
                            passwordOk.postValue(true); //heslo korektne
                        }
                        else {
                            //heslo nespravne
                            passwordOk.postValue(false);
                        }

                    }catch (Exception e){
                        passwordOk.postValue(false);
                        Log.e("Encryption key hash comparing error", e.getMessage());
                    }


                }
        );
    }


    private BigInteger fetchCurrentGasPrice(Web3j web3jClient) {

        try{

            EthGasPrice gasPrice = web3jClient.ethGasPrice().send();
            //konverzia na desatinne cislo v string podobe
           // currentGasPrice.postValue(convertToDecimalStr(gasPrice.getGasPrice()));
           // return convertToDecimalStr(gasPrice.getGasPrice());
            return gasPrice.getGasPrice();

        }catch (IOException ex){

           // currentGasPrice.postValue(getApplication().getString(R.string.way_summary_gasprice_error));
            return null;
        }
    }

    private BigInteger fetchCurrentBalance(Web3j web3jClient, String clientsAddress) {

        try {
            EthGetBalance balanceResponse = web3jClient
                    .ethGetBalance(clientsAddress, DefaultBlockParameterName.LATEST)
                    .send();
                   // .get(20, TimeUnit.SECONDS);

          //  BigInteger absolutBalance = balanceResponse.getBalance();

             //currentBalance.postValue(convertToDecimalStr(absolutBalance));
           // return convertToDecimalStr(absolutBalance);

            return balanceResponse.getBalance();
            //Log.d("Account balance", convertToDecimalStr(absolutBalance));

        } catch (IOException e) {

            Log.e("Eth balance exception", "Eth balance request exception error");
           // currentBalance.postValue(getApplication().getString(R.string.way_summary_balance_error));
            return null;

        }

    }

    // absolutne vyjadrenie bez desatin - int forma - BigInteger
    private String convertToDecimalStr(BigInteger absolutNum){

        //polygon aj ether je delitelny na 18 miest - vraciam desatinny hodnotu v hlavnej jednotke ether(MATIC)
        BigDecimal decimalBalance = new BigDecimal(absolutNum)
                .divide(new BigDecimal(1000000000000000000L), 18, RoundingMode.HALF_UP);

        return decimalBalance.toPlainString();      //plain string - bez vedeckej notacie (E)
    }

    private Boolean hasEnoughBalance(BigInteger balance, BigInteger gasPrice){

        if (balance.compareTo(tippedFee) >=0){   //fun dava 1 pre pripad vacsie a 0 pre pripad rovne

            return true;
        }
        return false;
    }

    private BigInteger countTippedFee(BigInteger weiGasPrice, BigDecimal TIP_CONST){
        //chcem nasobit - musim previest na decimal
        BigDecimal gasPriceDecimal = Convert.toWei(weiGasPrice.toString() ,Convert.Unit.WEI);

        //aby neodkrojilo desatiny, zaokruhlim - prevadzam na int - eth je delitelne len na 18 miest = wei
        gasPriceDecimal = gasPriceDecimal.multiply(TIP_CONST).setScale(0,RoundingMode.HALF_UP);
        //konverzia spat na wei
        return Convert.toWei(gasPriceDecimal.toPlainString(),Convert.Unit.WEI).toBigIntegerExact();
    }

    public void fetchTransactionInfo(String address){

        // pomocou hesla desifruj privatny kluc, nim podpis transakciu
        executor.execute(

                new Runnable() {
                    @Override
                    public void run() {

                        Web3j web3jClient = Web3JClient.getWeb3jClient();

                        //String user_address = getPickedAddress().getValue();
                        // jednotky su wei = 10^-18 eth
                       BigInteger gasPrice = fetchCurrentGasPrice(web3jClient);
                       BigInteger balance = fetchCurrentBalance(web3jClient, address);

                       if ((gasPrice==null) || (balance==null)){

                           currentBalance.postValue(getApplication().getString(R.string.way_summary_balance_error));
                           currentGasPrice.postValue(getApplication().getString(R.string.way_summary_gasprice_error));
                           enoughBalance.postValue(false);
                           return;
                       }

                        tippedFee = countTippedFee(gasPrice, TIP_CONST);

                        if (hasEnoughBalance(balance, tippedFee)){

                            enoughBalance.postValue(true);
                        }
                        else {
                            enoughBalance.postValue(false);
                        }

                        getCurrentGasPrice().postValue(convertToDecimalStr(tippedFee));
                        getCurrentBalance().postValue(convertToDecimalStr(balance));

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //UI work
                            }
                        });

                    }
                }
        );
    }

}
