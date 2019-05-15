package com.coolweather.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provincesList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

//    获取布局的实例
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.choose_area,container,false);
    titleText = view.findViewById(R.id.title_text);
    backButton = view.findViewById(R.id.back_button);
    listView = view.findViewById(R.id.list_view);
    adapter = new ArrayAdapter<>(getContext(),android.R.layout.
            simple_list_item_1,dataList);
    listView.setAdapter(adapter);
    return view;
    }

//    用来获取点击位置
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provincesList.get(position);
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(position);
                queryCounties();
            }
        });
        backButton.setOnClickListener(v -> {
            if(currentLevel == LEVEL_COUNTY){
                queryCities();
            }else if(currentLevel == LEVEL_CITY) {
                queryProvinces();
            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provincesList = DataSupport.findAll(Province.class);
        if(provincesList.size() > 0){

            dataList.clear();
            for(Province province : provincesList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            Log.d(TAG, "queryProvinces: kkk10");
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
            Log.d(TAG, "queryProvinces: kkk14");
        }
    }
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(
                selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    private  void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(
                selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }
//为了从服务器中获取数据
    private void queryFromServer(String address, final String type){
        showProgressDialog();

        Log.d(TAG, "queryFromServer: kkk333");
//        Callback是用来回调的
        HttpUtil.sendOkHttpRequest(address, new Callback() {
//            回调获得服务器中的数据，new Callback()只是声明了一个接口对象，对接口方法的实现在下面
//            为什么不能进入回调中
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.d(TAG, "onResponse: kkk202");
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyresponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(() -> {
                        closeProgressDialog();
                        if("province".equals(type)){
                            queryProvinces();
                        }else if("city".equals(type)){
                            queryCities();
                        }else if("county".equals(type)){
                            queryCounties();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void showProgressDialog(){
        if(progressDialog == null){
            Log.d(TAG, "showProgressDialog: kkk203");
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
