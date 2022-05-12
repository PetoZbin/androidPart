package com.example.orienteering.userAccess.accountPicker.pickerRecycler;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orienteering.databinding.AdressItemBinding;
import com.example.orienteering.R;
import com.example.orienteering.dbWork.registration.UserCredentials;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

public class PickerRecyclerAdapter extends RecyclerView.Adapter<PickerRecyclerAdapter.PickerViewHolder>{

    //items list
    private List<UserCredentials> itemList = new ArrayList<>();     //prazdny list

    //action listeners
    private OnDeleteListener onDeleteListener;
    private OnAccountPickedListener onAccountPickedListener;

    private com.example.orienteering.databinding.AdressItemBinding addressBinding;

    public List<UserCredentials> getItemList() {
        return itemList;
    }

    public void setItemList(List<UserCredentials> itemList) {

        int chosenAccIndex = -1;
        UserCredentials credentials = new UserCredentials();

        for (int i=0; i < itemList.size(); i++){

            if (itemList.get(i).isPicked){

                chosenAccIndex =i;
                credentials.setPicked(true);
                credentials.setUserId(itemList.get(i).getUserId());
                credentials.setAddress(itemList.get(i).getAddress());
                credentials.setPublicKey(itemList.get(i).getPublicKey());
                credentials.setPrivateHashed(itemList.get(i).getPrivateHashed());
                credentials.setId(itemList.get(i).getId());
                break;
            }
        }

        if (chosenAccIndex > 0){    // nulty netreba presunut, je na zaciatku

            itemList.remove(chosenAccIndex);
            itemList.add(0, credentials);
        }

        this.itemList = itemList;
    }

    public AdressItemBinding getAddressBinding() {
        return addressBinding;
    }

    public void setAddressBinding(AdressItemBinding addressBinding) {
        this.addressBinding = addressBinding;
    }


    public static class PickerViewHolder extends RecyclerView.ViewHolder{

        TextView addressField;
        ImageButton removeButton;

        public PickerViewHolder(@NonNull View itemView,
                                final OnDeleteListener onDeleteListener, final OnAccountPickedListener onAccountPickedListener ) {

            super(itemView);

            addressField = itemView.findViewById(R.id.address_text);
            addressField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (onAccountPickedListener != null){

                        int position = getAdapterPosition();    // pozicia polozky

                        if (position != RecyclerView.NO_POSITION){
                            onAccountPickedListener.onAccountPicked(position);  //posielam avizo, ze vybral dany prvok
                        }
                    }
                }
            });

            removeButton = itemView.findViewById(R.id.address_remove_btn);
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDeleteListener != null){

                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION){

                            onDeleteListener.onDelete(position);
                        }
                    }
                }
            });
        }
    }


    @NonNull
    @Override
    public PickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        this.addressBinding = AdressItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        View view = this.addressBinding.getRoot();
        return new PickerViewHolder(view, onDeleteListener, onAccountPickedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PickerViewHolder holder, int position) {

        UserCredentials item = this.itemList.get(position);
        holder.addressField.setText(item.getViewFormat());  //nastavim skratenu verziu adresy ex: 0xabcd....5dfe

        if (this.itemList.get(position).isPicked){
            holder.addressField.setTextColor(Color.parseColor("#047006"));
        }
    }

    @Override
    public int getItemCount() {     //pocet poloziek
        return this.itemList.size();
    }

    public interface OnDeleteListener{

        void onDelete(int position);
    }

    public interface OnAccountPickedListener{

        void onAccountPicked(int position);
    }

    public void setOnDeleteListener(OnDeleteListener listener){

        this.onDeleteListener = listener;
    }

    public void setOnAccountPickedListener(OnAccountPickedListener listener){

        this.onAccountPickedListener = listener;
    }
}
