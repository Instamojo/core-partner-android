package com.getmeashop.realestate.partner;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.getmeashop.realestate.partner.database.Cheque;

import java.util.ArrayList;

/**
 * Created by nikka on 9/8/15.
 */
public class UserChequeListAdapter extends RecyclerView.Adapter<UserChequeListAdapter.ViewHolder> {
    private ArrayList<Cheque> mDataset;
    private Context context;


    // Provide a suitable constructor (depends on the kind of dataset)
    public UserChequeListAdapter(Context context, ArrayList<Cheque> cheques) {
        this.context = context;
        this.mDataset = cheques;
    }

    public void editPaymentInfo(View view, int position) {
        Intent to_update = new Intent(context, EditPayment.class);
        to_update.putExtra("r_uri", mDataset.get(position).getR_uri());
        to_update.putExtra("image", mDataset.get(position).getImage());
        to_update.putExtra("cheque_num", mDataset.get(position).getNumber());
        to_update.putExtra("bank_name", mDataset.get(position).getBank_name());
        to_update.putExtra("deposited", mDataset.get(position).getDeposited());
        to_update.putExtra("plan", mDataset.get(position).getPlan());
        to_update.putExtra("amount_paid", mDataset.get(position).getAmount());
        to_update.putExtra("months", mDataset.get(position).getMonths());
        to_update.putExtra("start_date", mDataset.get(position).getStartDate());
        to_update.putExtra("position", position);
        context.startActivity(to_update);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserChequeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payment_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);

        vh.cheque_number = (TextView) v.findViewById(R.id.cheque_number);
        vh.bank_name = (TextView) v.findViewById(R.id.bank_name);
        vh.deposited = (TextView) v.findViewById(R.id.deposited);
        vh.image = (ImageView) v.findViewById(R.id.image);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (!mDataset.get(position).getNumber().equalsIgnoreCase("load+more")) {
            if (!mDataset.get(position).getNumber().equalsIgnoreCase("") &&
                    !mDataset.get(position).getNumber().equalsIgnoreCase("null"))
                holder.cheque_number.setText(mDataset.get(position).getNumber());
            if (!mDataset.get(position).getBank_name().equalsIgnoreCase("") &&
                    !mDataset.get(position).getBank_name().equalsIgnoreCase("null"))
                holder.bank_name.setText(mDataset.get(position).getBank_name());
            if (mDataset.get(position).getDeposited().equalsIgnoreCase("true"))
                holder.deposited.setText("Deposited");
            else
                holder.deposited.setText("Not Deposited");


            Utils.nullcase(holder.image, mDataset.get(position).getImage(), context);


            holder.cheque_number.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editPaymentInfo(holder.itemView, holder.getAdapterPosition());
                }
            });
        } else {
//            holder.loading.setVisibility(View.VISIBLE);
//            holder.details.setVisibility(View.GONE);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView cheque_number, bank_name, deposited;
        public ImageView image;

        public ViewHolder(View v) {
            super(v);
        }
    }


}
