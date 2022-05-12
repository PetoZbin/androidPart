package com.example.orienteering.dbWork.registration;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

import org.web3j.abi.datatypes.Bool;

@Entity(tableName = "user_credentials", indices = {

        @Index(value = "publicKey", unique = true),
        @Index(value = "address", unique = true)
})
public class UserCredentials {


        @PrimaryKey(autoGenerate = true)
        public Long id;

        @ColumnInfo(name = "userId")     //referencia na uzivatela
        public String userId;

        @ColumnInfo(name = "privateHashed")     //hashed private key
        public String privateHashed;

        @ColumnInfo(name = "publicKey")
        public String publicKey;

        @ColumnInfo(name = "address")       //eth address
        public String address;

        @ColumnInfo(name = "isPicked")       // currently picked account for the user
        public Boolean isPicked = false;

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getUserId() {
                return userId;
        }

        public void setUserId(String userId) {
                this.userId = userId;
        }

        public String getPrivateHashed() {
                return privateHashed;
        }

        public void setPrivateHashed(String privateHashed) {
                this.privateHashed = privateHashed;
        }

        public String getPublicKey() {
                return publicKey;
        }

        public void setPublicKey(String publicKey) {
                this.publicKey = publicKey;
        }

        public String getAddress() {
                return address;
        }

        public void setAddress(String address) {
                this.address = address;
        }

        public Boolean getPicked() {
                return isPicked;
        }

        public void setPicked(Boolean picked) {
                isPicked = picked;
        }


        public String getViewFormat(){

                //prvych 6 znakov + .... + posle 4 koncove znaky - user friendly zobrazenie

                return (this.address.substring(0,6) + "...." + this.address.substring(this.address.length() - 1 -3));

        }

}
