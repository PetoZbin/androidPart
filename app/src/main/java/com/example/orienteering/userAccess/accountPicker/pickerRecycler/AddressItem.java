package com.example.orienteering.userAccess.accountPicker.pickerRecycler;

public class AddressItem {

    private String address;

    public String getViewFormat(){

        //prvych 6 znakov + .... + posle 4 koncove znaky - user friendly zobrazenie

        return (this.address.substring(0,6) + "...." + this.address.substring(this.address.length() - 1 -3));

    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
