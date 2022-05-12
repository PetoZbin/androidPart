package com.example.orienteering.helpers;

import org.web3j.crypto.WalletUtils;

public class ValidationHelper {


    public static Boolean isPasswordValid(String password){

        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}"; //8 znakov, male, velke pismeno, bez medzery

        // nesplna regex
        return password.matches(pattern);
    }


    public static Boolean isLoginValid(String username){

        String pattern = "^\\d*[a-zA-Z][a-zA-Z0-9]*$";  //aspon jeden pismenovy character, pismena, cisla
        return username.matches(pattern);
    }

    public static Boolean isPrivateKeyValid(String privateKey){

        return WalletUtils.isValidPrivateKey(privateKey);

    }

    public static Boolean isAddressValid(String address){

        return WalletUtils.isValidAddress(address);
    }

    public static Boolean isEmailValid(String email){

        String pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        return email.matches(pattern);

    }





}
