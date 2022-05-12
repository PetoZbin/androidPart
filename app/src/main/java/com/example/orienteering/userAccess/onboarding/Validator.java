package com.example.orienteering.userAccess.onboarding;

import android.content.Context;

import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UsersDao;

import org.web3j.crypto.WalletUtils;

public abstract class Validator {

   public static Boolean isLoginValid(String userName){

        String pattern = "^\\d*[a-zA-Z][a-zA-Z0-9]*$";  //aspon jeden pismenovy character, pismena, cisla
        if (!userName.matches(pattern)){

            //zadaj error k polu

            return false;
        }

        return true;
    }

    public static Boolean isPrivKeyValid(String privateKey){

        if (!WalletUtils.isValidPrivateKey(privateKey)){

            return false;
        }
        return true;
    }

    public static Boolean isAddressValid(String address){

        return WalletUtils.isValidAddress(address);
    }

    public static Boolean isEmailValid(String email){

        String pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        if (!email.matches(pattern)){

            //error to textview
            return false;
        }

        return true;
    }


    public static Boolean isPasswordValid(String password){

        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}"; //8 znakov, male, velke pismeno, bez medzery

        if (!password.matches(pattern)){ // nesplna regex

            return false;
        }

        return true;    // splna regex
    }

    public static Boolean isPasswordCorrect(UsersDao dao){



        //nacitaj hash z db

        //db.usersDao().get


       return true;
    }

    public static Boolean isNumCompetitorsValid(String numCompetitors){


       try{
           if ((Integer.parseInt(numCompetitors) > 1)){
               return true;
           }
           else return false;
       }
       catch (NumberFormatException ex){

           return false;
       }
    }


    public static Boolean checkPasswords(String password, String passwordAgain){


        if ((!password.equals(passwordAgain)) || isPasswordValid(password)){

            return false;
        }

        return true;    // splna regex a heslo + heslo znova zhodne
    }

}
