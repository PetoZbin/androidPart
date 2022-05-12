package com.example.orienteering.dbWork.registration;

import android.net.Credentials;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Int;

import java.util.List;

@Dao
public interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUser(UserRegistration user);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCredentials(UserCredentials credentials);

    @Delete
    void deleteUser(UserRegistration user);

    @Delete
    void deleteCredentials(UserCredentials credentials);

    @Update
    void updateUser(UserRegistration user);


    @Transaction    //thread safe operacia
    @Query("SELECT * FROM user_table")
    public List<UserWithCredentials> getAllUsers();

    @Transaction
    @Query("SELECT * FROM user_table WHERE userId == :userId")
    public UserRegistration getUserById(String userId);

    @Transaction
    @Query("SELECT EXISTS(SELECT * FROM user_table WHERE userId == :userId LIMIT 1)")
    public Boolean isUserInTable(String userId);

    @Transaction
    @Query("SELECT * FROM user_table WHERE userId == :userId LIMIT 1")
    public UserWithCredentials getUserWithCredentialsById(String userId);

    @Transaction
    @Query("UPDATE user_table SET logged = 1, token = :token WHERE userId == :userId")
    public void logUserIn(String userId, String token);

    @Transaction
    @Query("UPDATE user_table SET logged = 0 WHERE userId == :userId")
    public void logUserOut(String userId);


    //kontroluje pritomnost adresy v db
    @Transaction
    @Query("SELECT EXISTS(SELECT * FROM user_credentials WHERE address == :address)")
    public Boolean checkAddressUsed(String address);


    //zmaz credentials na yaklade adresy
    @Transaction
    @Query("DELETE FROM user_credentials WHERE address == :address")
    public void deleteCredentialsByAddress(String address);

    // dotiahne vsetky ucty daneho uzivatela
    @Transaction
    @Query("SELECT * FROM user_credentials WHERE userId == :userId")
    public List<UserCredentials> getUserCredentials(String userId);

    @Transaction
    @Query("UPDATE user_table SET logged = 0")      //nastav vsetky ucty na logged out
    public void logAllOut();

    @Transaction
    @Query("SELECT * FROM user_table WHERE logged == 1")      //najdi prave prihlasenych uzivatelov
    public List<UserRegistration> getLoggedUsers();

    @Transaction
    @Query("SELECT * FROM user_table WHERE logged == 1 LIMIT 1")      //najdi prave prihlasenych uzivatelov
    public UserRegistration getLoggedUser();


    @Transaction
    @Query("DELETE FROM user_table WHERE username == :username")      //najdi prave prihlasenych uzivatelov
    public void removeUserByUsername(String username);


    //all accounts unpicked
    @Transaction
    @Query("UPDATE user_credentials SET isPicked = 0 WHERE userId == :userId")      //nastav vsetky ucty na logged out
    public void unpickAllUsersAccounts(String userId);

    @Transaction
    @Query("UPDATE user_credentials SET isPicked = 1 WHERE userId == :userId AND id == :accountId")      //nastav vsetky ucty na logged out
    public void pickUsersAccount(String userId, Long accountId);

    @Transaction
    @Query("SELECT COUNT(id) FROM user_credentials WHERE userId == :userId AND isPicked == 1")   //ci uz ma niektory ucet vybraty
    public Integer countAccountsPicked(String userId);

    @Transaction
    @Query("SELECT address FROM user_credentials WHERE userId == :userId AND isPicked == 1")
    public String getPickedUsersAccount(String userId);

    @Transaction
    @Query("SELECT * FROM user_credentials WHERE userId == :userId AND isPicked == 1 LIMIT 1")
    public UserCredentials getPickedUsersCredentials(String userId);

//    @Transaction
//    @Query("SELECT EXISTS(SELECT * FROM user_credentials WHERE userId == :userId AND isPicked == 1)")
//    public String getPickedUsersAccount(String userId);



}
