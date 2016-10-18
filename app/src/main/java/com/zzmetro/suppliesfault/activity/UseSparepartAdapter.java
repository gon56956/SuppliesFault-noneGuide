package com.zzmetro.suppliesfault.activity;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.zzmetro.suppliesfault.model.SparepartList;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mayunpeng on 16/8/3.
 */
public class UseSparepartAdapter extends BaseAdapter {

    private Context context;
    private List<SparepartList> sparepartList;
    private TextView faultInfo;
    private TextView totalNumber;
    private EditText etUseNumber;

    //定义一个HashMap，用来存放EditText的值，Key是position
    HashMap<Integer, String> hashMap = new HashMap<Integer, String>();

    public UseSparepartAdapter(Context context, List<SparepartList> sparepartList) {
        this.context = context;
        this.sparepartList = sparepartList;
    }

    @Override
    public int getCount() {
        return sparepartList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.activity_sparepart_use_item, null);
        faultInfo = (TextView) convertView.findViewById(R.id.faultInfo);
        totalNumber = (TextView) convertView.findViewById(R.id.totalNumber);
        etUseNumber = (EditText) convertView.findViewById(R.id.useNumber);

        etUseNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //将editText中改变的值设置的HashMap中
                hashMap.put(position, editable.toString());
            }
        });

        faultInfo.setText("\u3000" + sparepartList.get(position).getSparepartInfo());
        totalNumber.setText(sparepartList.get(position).getNumber());

        //如果hashMap不为空，就设置的editText
        if(hashMap.get(position) != null){
            etUseNumber.setText(hashMap.get(position));
        }

        return convertView;
    }
}
