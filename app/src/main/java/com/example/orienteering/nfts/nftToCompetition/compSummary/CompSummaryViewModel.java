package com.example.orienteering.nfts.nftToCompetition.compSummary;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.competition.CompetitionStates;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.crypto.NftManagerContract;
import com.example.orienteering.crypto.Web3JClient;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.dbWork.registration.UserCredentials;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UsersDao;
import com.example.orienteering.helpers.CipherHelper;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.CompetitionPattern;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.retrofit.patternClasses.LeaderboardWaypoint;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddResponse;
import com.example.orienteering.retrofit.responseClasses.login.LoginResponse;
import com.example.orienteering.retrofit.responseClasses.tokens.AddressNftsData;
import com.example.orienteering.userAccess.onboarding.responses.LoginFailResponse;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompSummaryViewModel extends LoggedViewmodel {

    public static final String LATEST_BLOCK = "latest";
    public static final String ADMIN_ETH_ADDRESS = "0x94C05d74f3E26474dd84D061e9CeA8F138a8f600"; //tu spravuje nfttokeny server
    //public static final String ADMIN_ETH_ADDRESS = "0x69C8454481b50075FB1E63a0f8f8e2b352a20f97"; //uzivatelsky ucet

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    private AddressNftsData chosenNft;

    private final UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();

    private MutableLiveData<String> compName = new MutableLiveData<String>(getApplication().getString(R.string.way_summary_def_comp_name));
    private MutableLiveData<String> maxCompetitors = new MutableLiveData<String>("0");
    private MutableLiveData<String> comDateTime = new MutableLiveData<String>("");
    private MutableLiveData<String> maxDuration = new MutableLiveData<String>();
    private MutableLiveData<String> currentGasPrice = new MutableLiveData<String>();
    private MutableLiveData<String> currentBalance = new MutableLiveData<String>();

    private MutableLiveData<ArrayList<CustomWaypointDesc>> waypoints
            = new MutableLiveData<ArrayList<CustomWaypointDesc>>(new ArrayList<CustomWaypointDesc>());



    private MutableLiveData<Boolean> passwordOk = new MutableLiveData<Boolean>(false);
    private MutableLiveData<Boolean> transactionSetOk = new MutableLiveData<Boolean>(false);
    private MutableLiveData<Boolean> receiptOk = new MutableLiveData<Boolean>(false);
    private MutableLiveData<CompetitionAddData> competitionAddResponse = new MutableLiveData<CompetitionAddData>();


    public MutableLiveData<Boolean> getPasswordOk() {
        return passwordOk;
    }

    public void setPasswordOk(MutableLiveData<Boolean> passwordOk) {
        this.passwordOk = passwordOk;
    }

    public MutableLiveData<Boolean> getTransactionSetOk() {
        return transactionSetOk;
    }

    public void setTransactionSetOk(MutableLiveData<Boolean> transactionSetOk) {
        this.transactionSetOk = transactionSetOk;
    }

    public MutableLiveData<Boolean> getReceiptOk() {
        return receiptOk;
    }

    public void setReceiptOk(MutableLiveData<Boolean> receiptOk) {
        this.receiptOk = receiptOk;
    }


    public MutableLiveData<String> getCompName() {
        return compName;
    }

    public void setCompName(MutableLiveData<String> compName) {
        this.compName = compName;
    }

    public CompSummaryViewModel(@NonNull Application application) {

        super(application);
        super.checkLogged(UserDatabase.getInstance(getApplication()));
    }



    public MutableLiveData<String> getMaxCompetitors() {
        return maxCompetitors;
    }

    public void setMaxCompetitors(MutableLiveData<String> maxCompetitors) {
        this.maxCompetitors = maxCompetitors;
    }

    public MutableLiveData<ArrayList<CustomWaypointDesc>> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String serializedWaypoints) {

        Type myType = new TypeToken<ArrayList<CustomWaypointDesc>>(){}.getType();

        this.waypoints.postValue((ArrayList<CustomWaypointDesc>) new Gson().fromJson(serializedWaypoints,myType));
    }

    public void setChosenNft(String serializedNft) {

        this.chosenNft = new Gson().fromJson(serializedNft, AddressNftsData.class); //deserializacia
    }

    public MutableLiveData<String> getComDateTime() {
        return comDateTime;
    }

    public void setComDateTime(MutableLiveData<String> comDateTime) {
        this.comDateTime = comDateTime;
    }

    public MutableLiveData<String> getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(MutableLiveData<String> maxDuration) {
        this.maxDuration = maxDuration;
    }

    public MutableLiveData<CompetitionAddData> getCompetitionAddResponse() {
        return competitionAddResponse;
    }

    public void updateDate(String dateTime){

        comDateTime.postValue(dateTime);

    }


    public void transferNft(String password, BigInteger tippedFee){

        // pomocou hesla desifruj privatny kluc, nim podpis transakciu
        executor.execute(

                new Runnable() {
                    @Override
                    public void run() {

                        try{

                            String pk = getDecryptedPrivate(password);
                            performTransaction(pk, tippedFee);

                        }catch (Exception e){
                            Log.e("Transaction problem","Transaction problem");
                        }

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

    private String getDecryptedPrivate(String encryptionKey) throws Exception {

        // udaje (public, private, adress) prave prihlaseneho klienta
        UserCredentials credentials = usersDao.getPickedUsersCredentials(getUserId().getValue());

        String encryptedPrivate = credentials.getPrivateHashed();  //nie hash ale AES sifrovany kluc

        return CipherHelper.decryptByAes(encryptionKey, encryptedPrivate);

    }

    //kurzy a tutorialy pouzite na zvladnutie technologie web3j + implementaciu: ↓
    //zaklad a implementacia klienta https://www.youtube.com/watch?v=fzUGvU2dXxU
    // ziskanie zostatku  https://www.youtube.com/watch?v=Cowuu7a5l-I

    private Boolean performTransaction(String privateKey, BigInteger tippedFee) {

//        // url uzla (endpoint) na pristup ku sieti - pouzivam moralis uzol pre mumbai testnet - polygon
//        Web3j web3jClient = Web3j.build(new HttpService("https://speedy-nodes-nyc.moralis.io/7e552ee0a5c1d070a33196ac/polygon/mumbai"));       // inicializacia web3j klienta - komunikacia s uzlom cez http
//        Web3ClientVersion web3ClientVersion = null;
//
//        try {
//            web3ClientVersion = web3jClient.web3ClientVersion().send();
//
//        }catch (IOException E){
//
//            Log.e("web3 client version: ", ".send IO exception");
//        }
//
//        if (web3ClientVersion != null){
//
//            String web3ClientVersionString = web3ClientVersion.getWeb3ClientVersion();
//            Log.d("Web3 client version: ", web3ClientVersionString);
//        }


       // String clientsAddress = getPickedAddress().getValue();
        String contractAddress = chosenNft.getTokenAddress();   //adresa, na ktorej bezi smart kontrakt

        Credentials credentials = Credentials.create(privateKey);

//        try {
//            EthGetBalance balanceResponse = web3jClient.ethGetBalance(clientsAddress, DefaultBlockParameter.valueOf(LATEST_BLOCK))
//                    .sendAsync()
//                    .get(20, TimeUnit.SECONDS);
//
//            //polygon aj ether je delitelny na 18 miest
//            BigInteger absolutBalance = balanceResponse.getBalance(); // absolutne vyjadrenie bez desatin - int forma
//            BigDecimal decimalBalance = new BigDecimal(absolutBalance)
//                    .divide(new BigDecimal(1000000000000000000L), 18, RoundingMode.HALF_UP);
//
//            Log.d("Account balance", decimalBalance.toString());
//
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//
//            Log.e("Eth balance exception", "Eth balance request exception error");
//        }

        //web3jClient.ethGetBalance(clientsAddress, DefaultBlockParameter.valueOf(LATEST_BLOCK)); //latest - z aktualne posledneho bloku

        // najskor skompiluj kontrakt cez solc - vutvori aj abi aj bin
        //solc .\diplomovka\allProjects\thesis\solc_scripts\erc_721_actual.sol --bin --abi -o .\diplomovka\allProjects\thesis\solc_scripts --allow-paths "C:/Users/peto/node_modules/"

        //potom vytvorim cez web3j command line java wrapper pre smart kontrakt, s ktorym web3j v mojej aplikacii naraba
        //web3j generate solidity D:\diplomovka\allProjects\thesis\solc_scripts\NftManagerContract.bin
        // D:\diplomovka\allProjects\thesis\solc_scripts\NftManagerContract.abi -o D:\diplomovka\allProjects\thesis\solc_scripts\


        try {

            Web3j web3jClient = Web3JClient.getWeb3jClient();

            String chainId = web3jClient.netVersion().send().getNetVersion();
            Log.d("ChainId", chainId);


            TransactionManager transactionManager = new RawTransactionManager(web3jClient,credentials,Long.parseLong(chainId));
            NftManagerContract contract = NftManagerContract.load(contractAddress,web3jClient,transactionManager,
                   tippedFee ,  DefaultGasProvider.GAS_LIMIT
                    );

           TransactionReceipt receipt =
                   contract.performTransfer(new BigInteger(chosenNft.getTokenId()), ADMIN_ETH_ADDRESS)
                           .send();

           if (receipt.isStatusOK()){
                //zadanie sutaze na server
               registerCompetition(receipt.getBlockHash()); // registruj sutaz
           }
           else {

               Log.e("Transaction problem", receipt.getStatus());
           }

           //vytvor sutaz v databaze

        } catch (IOException e) {

            Log.e("Eth balance exception", "Eth balance request exception error");
        } catch (Exception e) {
            Log.e("Transaction problem","Transaction problem");
            e.printStackTrace();
        }

        return true;
    }


    private void getCurrentGasPrice(Web3j web3jClient) {

        try{

            EthGasPrice gasPrice = web3jClient.ethGasPrice().send();

            currentGasPrice.postValue(gasPrice.getGasPrice().toString());

        }catch (IOException ex){

            currentGasPrice.postValue(getApplication().getString(R.string.way_summary_gasprice_error));

        }
    }

    private void getCurrentBalance(Web3j web3jClient, String clientsAddress) {

        try {
            EthGetBalance balanceResponse = web3jClient.ethGetBalance(clientsAddress, DefaultBlockParameter.valueOf(LATEST_BLOCK))
                    .sendAsync()
                    .get(20, TimeUnit.SECONDS);

            //polygon aj ether je delitelny na 18 miest
            BigInteger absolutBalance = balanceResponse.getBalance(); // absolutne vyjadrenie bez desatin - int forma
            BigDecimal decimalBalance = new BigDecimal(absolutBalance)
                    .divide(new BigDecimal(1000000000000000000L), 18, RoundingMode.HALF_UP);

            currentBalance.postValue(decimalBalance.toString());
            Log.d("Account balance", decimalBalance.toString());

        } catch (InterruptedException | ExecutionException | TimeoutException e) {

            currentBalance.postValue(getApplication().getString(R.string.way_summary_balance_error));
            Log.e("Eth balance exception", "Eth balance request exception error");
        }

    }

    //zavola sa na zaklade uspesneho prevodu NFT tokenu na serverovu adresu
    private void registerCompetition(String blockHash){


        CompetitionPattern competition = new CompetitionPattern();
        competition.setName(compName.getValue());
        competition.setMunicipality(waypoints.getValue().get(0).getMunicipality());
        competition.setOrganizerAddress(getPickedAddress().getValue());
        competition.setOrganizerId(getUserId().getValue());
        competition.setMaxCompetitors(Integer.parseInt(getMaxCompetitors().getValue()));
        competition.setCompDateTime(convertDateTime(getComDateTime().getValue()));
        competition.setDurationMins(Integer.parseInt(getMaxDuration().getValue()));
        competition.setMetaUrl(chosenNft.getImageUrl());
        competition.setNftId(chosenNft.getTokenId());
        competition.setNftName(chosenNft.getName());
        competition.setBlockHash(blockHash);
        competition.setWaypointList(waypoints.getValue());

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CompetitionAddResponse> call = apiInterface.postCompetition(competition);

        call.enqueue(new Callback<CompetitionAddResponse>() {
            @Override
            public void onResponse(Call<CompetitionAddResponse> call, Response<CompetitionAddResponse> response) {


                if (response.code() == 500){

                    Log.d("connection error", response.message());

                    CompetitionAddData errResponse = new CompetitionAddData();
                    errResponse.setMessage(response.message());
                    errResponse.setSuccess(false);

                    competitionAddResponse.postValue(errResponse);
                }
                else if(response.code() == 200){

                    Log.d("message", response.message());

                    CompetitionAddResponse addResponse = response.body();

                    CompetitionAddData addData = new CompetitionAddData();
                    addData.setCompetitionId(addResponse.getData().getCompetitionId());
                    addData.setMessage(getApplication().getString(R.string.way_summary_add_comp_success));
                    addData.setSuccess(true);

                    persistCreatedCompetitionToDb(competition, addData.getCompetitionId());
                    competitionAddResponse.postValue(addData);

                }
                else {
                    Log.d("connection error", "smt went wrong");

                    CompetitionAddData errResponse = new CompetitionAddData();
                    errResponse.setMessage(getApplication().getString(R.string.way_summary_add_comp_error));
                    errResponse.setSuccess(false);

                    competitionAddResponse.postValue(errResponse);

                }

            }

            @Override
            public void onFailure(Call<CompetitionAddResponse> call, Throwable t) {

                //chyba spojenia toast
                Log.d("connection error", "smt went wrong");

                CompetitionAddData errResponse = new CompetitionAddData();
                errResponse.setMessage(getApplication().getString(R.string.common_con_error));
                errResponse.setSuccess(false);

                competitionAddResponse.postValue(errResponse);
            }
        });
    }

    private void persistCreatedCompetitionToDb(CompetitionPattern createdComp, String newCompId){

        UserCompetition dbComp = new UserCompetition();
        dbComp.setCompetitionId(newCompId);
        dbComp.setCompetitorStatus(CompetitorStates.SPECTATOR.toString());
        dbComp.setCompetitionStatus(CompetitionStates.AWAITING.toString());
        dbComp.setLastCheckpointPos(0);
        dbComp.setCompDateTime(createdComp.getCompDateTime());
        dbComp.setDurationMins(createdComp.getDurationMins());
        dbComp.setUserId(createdComp.getOrganizerId());
        dbComp.setOwner(true);
        dbComp.setActive(false);
        dbComp.setAddress(createdComp.getOrganizerAddress());


        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(

            () -> {

                UserCompetition foundComp = db.competitionDao().getLoggedUserCompetitionById(newCompId);
                if (foundComp == null){    //nenasiel - vloz

                    db.competitionDao().insertCompetition(dbComp);

                }else {

                    // nasiel - update
                    dbComp.setId(foundComp.id);
                    db.competitionDao().updateCompetition(dbComp);
                }
            }
    );



    }

    private String convertDateTime(String skDateTime){

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

        try {

            Date datetime = sdf.parse(skDateTime);

            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            return sdf.format(datetime);

        }catch (ParseException | NullPointerException e){

            return null;
        }

    }

}
