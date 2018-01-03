package com.getmeashop.partner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getmeashop.partner.database.DatabaseHandler;
import com.getmeashop.partner.database.User;
import com.getmeashop.partner.util.Constants;
import com.getmeashop.partner.util.Interfaces;

import java.util.ArrayList;

/**
 * Created by nikka on 9/8/15.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    private ArrayList<User> mDataset, mVisibleDataset;
    private Context context;
    private Fragment frag;
    private ItemFilter mFilter = new ItemFilter();


    // Provide a suitable constructor (depends on the kind of dataset)
    public UserListAdapter(Context context, ArrayList<User> users, Fragment frag) {
        this.context = context;
        this.mDataset = users;
        this.mVisibleDataset = users;
        this.frag = frag;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (!mVisibleDataset.get(position).getUsername().equalsIgnoreCase("load+more")) {
            holder.details.setVisibility(View.VISIBLE);
            holder.loading.setVisibility(View.GONE);
            if (!mVisibleDataset.get(position).getUsername().equalsIgnoreCase("") &&
                    !mVisibleDataset.get(position).getUsername().equalsIgnoreCase("null"))
                holder.title.setText(mVisibleDataset.get(position).getUsername());
            if (!mVisibleDataset.get(position).getEmail().equalsIgnoreCase("") &&
                    !mVisibleDataset.get(position).getEmail().equalsIgnoreCase("null"))
                holder.email.setText(mVisibleDataset.get(position).getUsername() + Constants.base_suffix);

        } else {
            holder.loading.setVisibility(View.VISIBLE);
            holder.details.setVisibility(View.GONE);
        }

        if (mVisibleDataset.get(position).getIsActv().equalsIgnoreCase("true")) {
            holder.dlt_user.setText("Deactivate");
            holder.dlt_user.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        } else {
            holder.dlt_user.setText("Activate");
            holder.dlt_user.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_name, 0, 0, 0);
        }

        if (mVisibleDataset.get(position).getIsArchv()) {
            holder.archive.setText("Unarchive");
        } else {
            holder.archive.setText("Archive");
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVisibleDataset.size();
    }

    public void setFilter(String queryText) {
        queryText = queryText.toLowerCase();
        mVisibleDataset = mDataset;
        mFilter.filter(queryText);
    }

    public void setFilter(String queryText, ArrayList<User> users) {
        queryText = queryText.toLowerCase();
        mVisibleDataset = users;
        mDataset = users;
        mFilter.filter(queryText);
    }

    public void resetUsers(ArrayList<User> users) {
        mVisibleDataset = users;
        mDataset = users;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView title, email;
        public Button edit_pay, edit_store, dlt_user, archive;
        public RelativeLayout details, loading;

        public ViewHolder(View v) {
            super(v);

            details = (RelativeLayout) v.findViewById(R.id.details);
            loading = (RelativeLayout) v.findViewById(R.id.loading);
            dlt_user = (Button) v.findViewById(R.id.delete_user);
            archive = (Button) v.findViewById(R.id.archive);
            title = (TextView) v.findViewById(R.id.title);
            email = (TextView) v.findViewById(R.id.txtEmail);
            edit_store = (Button) v.findViewById(R.id.edit_store_info);
            edit_pay = (Button) v.findViewById(R.id.edit_pay_info);

            title.setOnClickListener(this);
            edit_pay.setOnClickListener(this);
            dlt_user.setOnClickListener(this);
            archive.setOnClickListener(this);
            edit_store.setOnClickListener(this);
            email.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.delete_user) {
                delete(getAdapterPosition());
            } else if (v.getId() == R.id.edit_store_info) {
                editStoreInfo(getAdapterPosition());
            } else if (v.getId() == R.id.edit_pay_info) {
                editPaymentInfo(getAdapterPosition());
            } else if (v.getId() == R.id.archive) {
                archive(getAdapterPosition());
            } else if (v.getId() == R.id.title) {
                editUserInfo(getAdapterPosition());
            } else if (v.getId() == R.id.txtEmail) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + mVisibleDataset.get(getAdapterPosition()).getUsername() + Constants.base_suffix));
                context.startActivity(browserIntent);
            }
        }


        public void editStoreInfo(int position) {
            Intent to_update = new Intent(context, EditInfo.class);
            to_update.putExtra("id", mVisibleDataset.get(position).getPid());
            to_update.putExtra("storeid", mVisibleDataset.get(position).getStoreinfo());
            context.startActivity(to_update);
        }


        public void editUserInfo(int position) {
            Intent to_update = new Intent(context, UpdateUser.class);
            to_update.putExtra("id", mVisibleDataset.get(position).getId());
            ((Interfaces.shouldNotify) frag).shouldNotify();
            context.startActivity(to_update);

        }


        public void editPaymentInfo(int position) {
            Intent to_update = new Intent(context, UserChequeList.class);
            to_update.putExtra("id", mVisibleDataset.get(position).getPid());
            context.startActivity(to_update);
        }

        public void archive(int position) {

            DatabaseHandler dbh = new DatabaseHandler(context);

            if (mVisibleDataset.get(position).getIsArchv()) {
                mVisibleDataset.get(position).setIsArchv(false);
                dbh.unArchiveUser(mVisibleDataset.get(position).getPid());
            } else {
                mVisibleDataset.get(position).setIsArchv(true);
                dbh.ArchiveUser(mVisibleDataset.get(position).getPid());
            }


            ((Interfaces.archive) frag).archive(mVisibleDataset.get(position));
            //mVisibleDataset.remove(position);
            //mDataset.remove(position);

            notifyItemRemoved(position);

            if (mVisibleDataset.size() > 1) {
                notifyItemRangeChanged(position, mVisibleDataset.size());
            }
        }


        public void delete(int position) {
            ((Interfaces.archive) frag).delete(mVisibleDataset.get(position), position);
        }


    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            String filterable[];

            if (filterString.contains(" ")) {
                filterable = filterString.split(" ");
            } else {
                filterable = new String[1];
                filterable[0] = filterString;
            }

            FilterResults results = new FilterResults();

            final ArrayList<User> list = mVisibleDataset;

            int count = list.size();
            final ArrayList<User> nlist = new ArrayList<User>(count);


            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getEmail() + " " + list.get(i).getUsername();
                Boolean found = true;
                for (int k = 0; k < filterable.length; k++) {
                    if (!filterableString.toLowerCase().contains(filterable[k])) {
                        found = false;
                    }
                }
                if (found) {
                    nlist.add(list.get(i));
                }
            }

            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mVisibleDataset = (ArrayList<User>) results.values;
            notifyDataSetChanged();
            ((Interfaces.SearchResultCount) (frag)).SearchItemsCount(results.count);
        }

    }
}
