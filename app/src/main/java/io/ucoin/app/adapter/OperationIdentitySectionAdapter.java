package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.ucoin.app.Format;
import io.ucoin.app.R;
import io.ucoin.app.enumeration.TxDirection;
import io.ucoin.app.enumeration.TxState;
import io.ucoin.app.sqlite.SQLiteView;

public class OperationIdentitySectionAdapter extends CursorAdapter {

    private Context mContext;
    private Cursor mCursor;
    private int commentIndex;
    private int directionIndex;
    private int stateIndex;
    private int walletIdIndex;
    private int timeIndex;
    private int currencyNameIndex;
    private int amountIndex;
    private int dtIndex;
    private int dividendIndex;
    private int dividendThenIndex;

    private HashMap<Integer, String> mSectionPosition;

    public OperationIdentitySectionAdapter(Context context, Cursor cursor) {
        super(context,cursor,0);
        //super(context, R.layout.list_item_tx,new ArrayList<UcoinTx>());
        this.mContext = context;
        this.mCursor = cursor;
        this.mSectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        //swapCursor(cursor,wallet);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (mSectionPosition.containsKey(position)) {
            v = newSectionView(mContext, parent);
            bindSectionView(v, mContext, mSectionPosition.get(position));
        } else {
            v = newView(mContext, getCursor(),parent);
            getCursor().moveToPosition(getRealPosition(position));
            bindView(v, mContext, getCursor());
        }
        return v;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_item_tx, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.day = (TextView) view.findViewById(R.id.day);
        holder.hour = (TextView) view.findViewById(R.id.hour);
        holder.amount = (TextView) view.findViewById(R.id.amount);
        holder.defaultAmount = (TextView) view.findViewById(R.id.second_amount);
        holder.comment = (TextView) view.findViewById(R.id.comment);

        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        Long time = cursor.getLong(timeIndex);
        Date date = new Date(time*1000);
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        holder.day.setText(new SimpleDateFormat("EEE dd").format(c.getTime()));
        holder.hour.setText(new SimpleDateFormat("hh:mm:ss").format(c.getTime()));
        String dir;
        if (cursor.isNull(directionIndex) ||
                TxDirection.valueOf(cursor.getString(directionIndex)) == TxDirection.IN) {
            dir = "+ ";
        } else {
            dir = "- ";
        }
        String value = cursor.getString(amountIndex);
        //TODO deuxieme passage paveur à null
        Format.Currency.changeUnit(
                context,
                cursor.getString(currencyNameIndex),
                new BigInteger(value == null ? "0" : value),
                new BigInteger(cursor.getString(dividendIndex)),
                cursor.getInt(dtIndex),
                holder.amount,
                holder.defaultAmount,
                dir);
        holder.comment.setText(cursor.getString(commentIndex));
        if (cursor.isNull(stateIndex)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.primaryLight));
        } else if (TxState.valueOf(cursor.getString(stateIndex)) == TxState.CONFIRMED) {
            view.setBackgroundColor(context.getResources().getColor(R.color.grey200));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.accentLight));
        }
    }

    public View newSectionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_section_separator, parent, false);
    }

    public void bindSectionView(View v, Context context, String section) {
        ((TextView) v.findViewById(R.id.section_name)).setText(section);
    }

    public Cursor swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);

        if (newCursor == null) {
            return null;
        }

        timeIndex = newCursor.getColumnIndex(SQLiteView.Tx.TIME);
        amountIndex = newCursor.getColumnIndex(SQLiteView.Tx.AMOUNT);
        directionIndex = newCursor.getColumnIndex(SQLiteView.Tx.DIRECTION);
        currencyNameIndex = newCursor.getColumnIndex(SQLiteView.Tx.CURRENCY_NAME);
        dtIndex = newCursor.getColumnIndex(SQLiteView.Tx.CURRENCY_DT);
        dividendIndex = newCursor.getColumnIndex(SQLiteView.Tx.CURRENCY_DIVIDEND);
        dividendThenIndex = newCursor.getColumnIndex(SQLiteView.Tx.DIVIDEND);
        commentIndex =newCursor.getColumnIndex(SQLiteView.Tx.COMMENT);
        stateIndex = newCursor.getColumnIndex(SQLiteView.Tx.STATE);
        walletIdIndex = newCursor.getColumnIndex(SQLiteView.Tx.WALLET_ID);

        mCursor = newCursor;
        mSectionPosition.clear();
        int position = 0;
        String section = "";


        HashMap<Integer, String> sectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        if(newCursor.moveToFirst()){
            do{
                Long time = newCursor.getLong(timeIndex);
                Date date = new Date(time*1000);
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                String newSection = new SimpleDateFormat("MMMM yyyy").format(c.getTime());
                if (!newSection.equals(section)) {
                    sectionPosition.put(position, newSection);
                    section = newSection;
                    position++;
                }
                position++;
            }while (newCursor.moveToNext());
        }

        mSectionPosition = sectionPosition;
        notifyDataSetChanged();

        return newCursor;
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(getRealPosition(position));
    }

    public int getRealPosition(int position){
        int nbSec = 0;
        for (Integer i : mSectionPosition.keySet()) {
            if (position > i) {
                nbSec += 1;
            }
        }
        position -= nbSec;
        return position;
    }

    @Override
    public int getCount() {
        return super.getCount() + mSectionPosition.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return !mSectionPosition.containsKey(position);
    }

    private static class ViewHolder {
        public TextView day;
        public TextView hour;
        public TextView amount;
        public TextView defaultAmount;
        public TextView comment;
    }

}