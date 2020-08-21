package com.example.sunnywheather.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sunnywheather.R;
import com.example.sunnywheather.db.City;
import com.example.sunnywheather.db.County;
import com.example.sunnywheather.db.Province;
import com.example.sunnywheather.util.HttpUtil;
import com.example.sunnywheather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

import okhttp3.Response;
// 新建一个ChooseAreaFragment用于展示查询界面和实现基本查询功能
public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    /*** 省列表 */
    private List<Province> provinceList;
    /*** 市列表 */
    private List<City> cityList;
    /*** 县列表 */
    private List<County> countyList;
    /*** 选中的省份 */
    private Province selectedProvince;
    /*** 选中的城市 */
    private City selectedCity;
    /*** 当前选中的级别*/
    private int currentLevel;

    /**
     * 在onCreateView()方法中先是获取到了一些控件的实例，
     * 然后去初始化了ArrayAdapter，并将它设置为ListView的适配器。
     * 接着在onActivityCreated() 方法中给ListView和Button设置了点击事件，
     * 到这里我们的初始化工作就算是完成了
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    /**
     * 在onActivityCreated()方法中设置ListView和Button的点击事件，
     * 在这里完成了基本的初始化操作，调用queryProvinces()方法，加载省级数据
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //对列表设置监听事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    //记住选中的省份
                    selectedProvince = provinceList.get(position);
                    //显示出省份对应下city的界面
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    //记住选中的City
                    selectedCity = cityList.get(position);
                    //切换到相应的county界面
                    queryCounties();
                }
            }
        });
        //为返回按钮注册监听事件
        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //若在county切换到City
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    //若在City切换到province
                    queryProvinces();
                }
            }
        });
        //初始状态下显示province
        queryProvinces();
    }

    /*** 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询 */
    private void queryProvinces() {
        //首先将头布局的标题设为中国
        titleText.setText("中国");
        //隐藏返回按键
        backButton.setVisibility(View.GONE);
        //调用LitePal接口读取省级数据
        provinceList = LitePal.findAll(Province.class);
        //如果读取到了就直接将数据显示在桌面上
        if (provinceList.size() > 0) {
            dataList.clear();
            //遍历链表
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            //否则向服务器上查询
            String address = "http://guolin.tech/api/china";
            //传入一个地址和回调函数
            queryFromServer(address, "province");
        }
    }

    /*** 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询 */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /*** 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询 */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /*** 根据传入的地址和类型从服务器上查询省市县数据 */
    /*根据传入的地址和类型从服务器上获取数据
     * */
    private void queryFromServer(String address,final String type){
        //未查出之前显示出进度条框
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if(type.equals("province")){
                    result = Utility.handleProvinceResponse(responseText);
                }else if(type.equals("city")){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if(type.equals("county")){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if(type.equals("province")){
                                queryProvinces();
                            }else if(type.equals("city")){
                                queryCities();
                            }else if(type.equals("county")){
                                queryCounties();
                            }
                        }
                    });
                }


            }
        });
    }

    /*** 显示进度对话框 */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*** 关闭进度对话框 */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}