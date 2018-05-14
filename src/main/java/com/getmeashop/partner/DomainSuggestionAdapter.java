package com.getmeashop.partner;

/**
 * Created by naveenkumar on 17/02/16.
 */


    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ArrayAdapter;
    import android.widget.TextView;

    import java.util.ArrayList;

public class DomainSuggestionAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;

    public DomainSuggestionAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.domain_suggestions_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.domain_suggestions_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.domain_suggestion);
        TextView book = (TextView) rowView.findViewById(R.id.domain_reserve);
        textView.setText(values.get(position));


        return rowView;
    }

    @Override
    public int getCount() {
        return values.size();
    }
}