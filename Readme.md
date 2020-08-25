# 天气预报App

![1233](X:\Users\xu\Desktop\1233.png)



参考《第一行代码》，开发出一款全国省市县的天气预报app.

#### 一、使用LitePal创建数据库和表

使用**LitePal**对数据库进行操作，创建三个实体类分别是Province、City和County。

###### 1. 添加依赖项

```bash
compile 'org.litepal.android:core:1.3.2'
```

###### 2. 创建实体类

```java
package com.example.stardream.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by StarDream on 2018/8/22.
 */
//LitePal中的每一个实体类都应该继承DataSupport
public class Province extends DataSupport {
    private int id;  //实体类具有的id
    private String provinceName;  //省份的名字
    private int provinceCode;  //省的代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
```

City实体类和County实体类同理。每个实体类代表一张表，实体类中的属性代表表中的每一列。

###### 3. 配置litepal.xml文件

```xml
<litepal>
    <!--dbname 为数据库的名字-->
    <dbname value="cool_weather"/>
    <!--数据库版本指定为1-->
    <version value="1"/>
    <!--将实体类添加到映射列表中-->
    <list>
        <mapping class="com.example.stardream.coolweather.db.Province"/>
        <mapping class="com.example.stardream.coolweather.db.City"/>
        <mapping class="com.example.stardream.coolweather.db.County"/>
    </list>
</litepal>
```

###### 4. 修改AndroidManifest.xml文件

将项目的application配置为org.litepal.LitePalApplication



```bash
android:name="org.litepal.LitePalApplication"
```

关于LitePal的具体内容详见[LitePal详解](https://www.jianshu.com/p/6d3a0f87d637)

#### 二、遍历全国省市县数据

##### 1. 客户端与服务器的交互



```cpp
package com.example.stardream.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by StarDream on 2018/8/22.
 */
//采用OkHttp与服务器进行通信
public class HttpUtil {
    //与服务器进行交互发起一条http请求只需要调用sendOkHttpRequest()即可
    //传入要请求的地址，注册一个回调来处理服务器的响应
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request =  new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
```

发起http请求只需调用sendOkHttprequest()这个方法，传入一个地址，并且注册一个回调来处理服务器的响应。

##### 2. 处理服务器返回的Json格式的数据

新建一个Utility类处理和解析Json数据。



```java
package com.example.stardream.coolweather.util;

import android.text.TextUtils;

import com.example.stardream.coolweather.db.City;
import com.example.stardream.coolweather.db.County;
import com.example.stardream.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by StarDream on 2018/8/22.
 */

public class Utility {
    //处理和解析省份的数据
    public static boolean hanldeProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                //json的对象的数组，用来接收传回的多个省份的数据
                JSONArray allProvinces = new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    //取出每一个省份
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    //解析出省份的id并将其赋值给province对象
                    province.setProvinceCode(provinceObject.getInt("id"));
                    //解析出省份的name并将其赋值给province对象
                    province.setProvinceName(provinceObject.getString("name"));
                    //将这一个省份保存到表中
                    province.save();
                }
                //处理成功返回真
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        //处理失败返回假
        return false;
    }
    //处理和解析市的数据
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCity = new JSONArray(response);
                for(int i=0;i<allCity.length();i++){
                    JSONObject cityObject = allCity.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }

            }catch(JSONException e){
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
    //处理和解析县的数据
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounty = new JSONArray(response);
                for(int i=0;i<allCounty.length();i++){
                    JSONObject countyObject = allCounty.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setCityId(cityId);
                    county.setWeatherId(countyObject.getInt("weather_id"));
                    county.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
```

省级、市级和县级对服务器发来的数据的处理解析方式都是类似的，使用**JSONArray**和**JSONObject**进行解析，然后组装成**实体类对象**，再调用**save()**方法存储到数据库中。

##### 3. 遍历省市县

###### 遍历省市县功能.xml

因为遍历省市县的功能会用到多次，因此将其写为**碎片**的形式而不是写在活动里面，这样复用的时候可以直接在布局文件中调用碎片。

```xml
<?xml version="1.0" encoding="utf-8"?>
<!--头布局作为标题栏-->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#fff">
    <!--布局高度为actionBar高度，背景色为colorPrimary-->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary">
   <!--用于显示标题内容-->
        <TextView
            android:id="@+id/title_text"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:textColor="#fff"
            android:textSize="20sp"/>
        <!--返回按钮-->
        <Button
            android:id="@+id/back_button"
            android:layout_width="25dp"
            android:layout_height="25dp"
            android:layout_marginLeft="10dp"
            android:layout_alignParentLeft="true"
            android:layout_centerVertical="true"
            android:background="@drawable/ic_back"/>
        <!--省市县的数据显示在这里-->
        <!--listView会自动给每个子项之间增加一条分割线-->
        <ListView
            android:layout_width="match_parent"
            android:layout_height="match_parent">
            android:id="@+id/list_view/>
    </RelativeLayout>
</LinearLayout>
```

一般情况下标题栏可以采用ActionBar,但是碎片中最好不用ActionBar或Toolbar，否则会有问题。

###### 遍历省市县碎片

1. 新建一个ChooseAreaFragment用于展示查询界面和实现基本查询功能，定义一些量值。

```java
public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY =2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;//适配器，与ListView配合使用
    private List<String> dataList = new ArrayList<>();//动态数组
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;
}
```

2. onCreateView()方法中获取到一些控件的实例，初始化了ArrayAdapter，将其设置为ListView的适配器。

```java
   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
        * 【LayoutInflater】其实是在res/layout/下找到xml布局文件，并且将其实例化，
        * 对于一个没有被载入或者想要动态载入的界面，都需要使用LayoutInflater.inflate()来载入；
        * */
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        //载入listView
        listView.setAdapter(adapter);
        return view;
    }
```

3. 在onActivityCreated()方法中设置ListView和Button的点击事件，在这里完成了基本的初始化操作，调用queryProvinces()方法，加载省级数据。



```java
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
```

4. 在queryProvinces(）方法中，设置头布局的标题，返回按钮。调用LitePal查询结构读取省级数据，若读取到则将其显示在界面上，若没有则调用queryServer()方法从服务器查询数据。queryCities(）方法和queryCounty()方法同理。

- 查询省级数据



```java
/*查询全国所有的省，先从数据库查，没有的话去服务器查询
    * */
    private void queryProvinces(){
        //设置标题栏
        titleText.setText("中国");
        //隐藏返回按钮
        backButton.setVisibility(View.GONE);
        //在数据库中查询所有省份
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            //更新适配器中的内容，变为省份数据
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            //从服务器中查询
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
```

- 查询市级数据



```java
/*查询对应省的City数据，优先从数据库查，若没有，则去服务器查询
    * */
    private void queryCities(){
        //设置标题栏
        titleText.setText(selectedProvince.getProvinceName());
        //设置返回按钮可见
        backButton.setVisibility(View.VISIBLE);
        //在数据库中查询对应的City数据
        cityList = DataSupport.findAll(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else{
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryFromServer(address,"city");
        }
    }
```

- 查询县级数据



```java
 /*查询选中的市内的所有县，优先从数据库查，若没有则去服务器查询
    * */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.findAll(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            String address = "http://guolin.tech/api/china/"+
                    selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            queryFromServer(address,"county");
        }
    }
```

5. 在queryFromServer()方法中，调用HttpUtil中的sendOkHttpRequest()方法向服务器发送请求，响应的数据回调到onResponse(）方法中，然后调用Utility中的handleProvincesResponse()方法解析和处理服务器返回的数据，然后将其存储到数据库中。最后再次调用queryProvinces()方法重新加载省级数据，因为**queryProvinces()涉及UI操作**，则须在主线程中调用，因此借助**runOnUiThread()方法从子线程切换到主线程**。

- （***问题：queryFromServer()在哪里说明是是在子线程中执行的???\***）



```tsx
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
                    result = Utility.hanldeProvinceResponse(responseText);
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
```

#### 6. 涉及到的进度条框

- 进度条框的显示



```csharp
    //显示进度条框
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载…");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
```

- 进度条框的关闭



```csharp
    //关闭进度框
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
```

### 三、将碎片添加到活动

#### 1. 修改activity_main.xml中的代码

定义一个FrameLayout，将ChooseAreaFragment加入，并让其充满整个布局。



```xml
 <FrameLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <fragment
            android:id="@+id/choose_area_fragment"
            android:name="com.example.stardream.coolweather.activity.ChooseAreaFragment"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
    </FrameLayout>
```

#### 2. 删除原生的ActionBar

在style.xml中，



```xml
<style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
```

#### 3. 权限问题

在AndroidManifest.xml中添加网络权限。



```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

### 三、显示天气信息

因为和风天气返回的天气信息的JSON数据结构非常复杂，若使用JSONObject解析比较麻烦，因此使用GSON对天气信息进行解析。
 ***在这里应该加入JSONObject和GSON解析和处理数据的区别\***

#### 1. 定义GSON实体类

返回天气信息的格式：



![img](https:////upload-images.jianshu.io/upload_images/13122291-c81e0adc18155a5c.png?imageMogr2/auto-orient/strip|imageView2/2/w/291/format/webp)

返回数据的格式



要为basic、aqi、now、suggestion和daily_forecase定义实体类。

###### basic

![img](https:////upload-images.jianshu.io/upload_images/13122291-a71d9cdf4b1d2d88.png?imageMogr2/auto-orient/strip|imageView2/2/w/291/format/webp)

basic具体内容



在gson包下建立Basic类



```kotlin
package com.example.stardream.coolweather.gson;
import com.google.gson.annotations.SerializedName;
/**
 * Created by StarDream on 2018/8/24.
 */
/*由于JSON中的一些字段不太适合直接作为Java字段命名，
这里使用@SerializedName朱姐的方式让JSON字段和java字段建立映射关系
* */
public class Basic {
    //"city"与cityName建立映射关系
    @SerializedName("city")
    public String cityName;
    
    //"id"与weatherId建立映射关系
    @SerializedName("id")
    public String weatherId;
    
    @SerializedName("update")
    public Update update;
    public class Update{
        //"loc"与updateTime建立映射关系
        @SerializedName("loc")
        public String updateTime;
    }
}
```

###### aqi

![img](https:////upload-images.jianshu.io/upload_images/13122291-a24960829e0245d1.png?imageMogr2/auto-orient/strip|imageView2/2/w/182/format/webp)

aqi具体内容



```kotlin
package com.example.stardream.coolweather.gson;
import com.google.gson.annotations.SerializedName;
/**
 * Created by StarDream on 2018/8/24.
 */
public class AQI {
    public AQICity city;
    public class AQICity{
        @SerializedName("aqi")
        String aqi;
        
        @SerializedName("pm25")
        String pm25;
    }
}
```

***为什么这里的“aqi”与“pm25”没有使用SerilizedName???\***

###### now

![img](https:////upload-images.jianshu.io/upload_images/13122291-56e74a92b8d4ce16.png?imageMogr2/auto-orient/strip|imageView2/2/w/206/format/webp)

now具体内容



```kotlin
package com.example.stardream.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by StarDream on 2018/8/24.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
```

###### suggestion

![img](https:////upload-images.jianshu.io/upload_images/13122291-1a9657be9d2a814b.png?imageMogr2/auto-orient/strip|imageView2/2/w/561/format/webp)

suggestion



```kotlin
package com.example.stardream.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by StarDream on 2018/8/24.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    @SerializedName("sport")
    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }
    public class CarWash{
        @SerializedName("txt")
        public String info;
    }
    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
```

###### daily_forecast

![img](https:////upload-images.jianshu.io/upload_images/13122291-141be1ad27cff503.png?imageMogr2/auto-orient/strip|imageView2/2/w/242/format/webp)

daily_forecast具体内容



daily_forecase内包含的是一个数组，只定义出单日天气的实体类，在声明实体类引用的时候使用集合类型来进行声明。
 package com.example.stardream.coolweather.gson;
 import com.google.gson.annotations.SerializedName;
 /**

- Created by StarDream on 2018/8/24.
  */

public class Forecast {
 @SerializedName("date")
 public String date;



```kotlin
@SerializedName("tmp")
public Temperature temperature;

@SerializedName("cond")
public More more;

public class Temperature{
    @SerializedName("max")
    public String max;
    
    @SerializedName("min")
    public String min;
}
public class More{
    @SerializedName("txt_d")
    public String info;
}
```

}

###### 总实体类Weather

创建一个总的实体类来引用印上的各个实体类。



```kotlin
package com.example.stardream.coolweather.gson;
import com.google.gson.annotations.SerializedName;
import java.util.List;
/**
 * Created by StarDream on 2018/8/24.
 */
public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    //由于daily_forecase中包含的是一个数组，
    //这里使用List集合来引用Forecast类
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
```

#### 2. 编写天气界面

天气信息界面，activity_weather.xml

为了让代码相对整齐，采用**引用布局技术**，将界面的不同部分写在不同的文件中，再通过引入布局的方式集成到activity_weather.xml中。

#### title.xml头布局

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize">
    <TextView
        android:id="@+id/title_city"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:textColor="#fff"
        android:textSize="20sp"/>
    <TextView
        android:id="@+id/title_update_time"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginRight="10dp"
        android:layout_alignParentRight="true"
        android:layout_centerVertical="true"
        android:textColor="#fff"
        android:textSize="16sp"/>

</RelativeLayout>
```

其中，一个居中显示城市名，一个居右显示更新时间。

#### now.xml当前天气信息布局



```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15dp">
    <TextView
        android:id="@+id/degree_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end"
        android:textColor="#fff"
        android:textSize="60sp"/>
    <TextView
        android:id="@+id/weather_info_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end"
        android:textColor="#fff"
        android:textSize="20sp"/>

</LinearLayout>
```

其中，放置了两个textView,一个用来显示气温，另外一个用于显示天气概况。

#### forecast.xml未来几天天气信息的布局



```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15dp"
    android:background="#8000">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginLeft="15dp"
        android:layout_marginTop="15dp"
        android:text="预报"
        android:textColor="#fff"
        android:textSize="20sp"/>
    <LinearLayout
        android:id="@+id/forecast_layout"
        android:layout_width="match_parent"
        android:orientation="vertical"
        android:layout_height="wrap_content">
    </LinearLayout>

</LinearLayout>
```

布局使用了半透明背景，TextView定义了标题“预报”，使用LinearLayout定义了一个显示未来几天天气的布局，但未放入内容，要根据服务器返回的数据在代码中动态添加。

#### forecast_item.xml未来天气信息的子项布局



```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15dp">
    <TextView
        android:id="@+id/date_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_weight="2"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/info_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_weight="1"
        android:gravity="center"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/max_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_weight="1"
        android:gravity="right"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/min_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_weight="1"
        android:gravity="left"
        android:textColor="#fff"/>
    

</LinearLayout>
```

子项布局中放置了4个textView,分别用于显示天气预报日期，天气概况，最高温度和最低温度。

#### aqi空气质量信息的布局



```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15dp"
    android:background="#8000">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginLeft="15dp"
        android:layout_marginTop="15dp"
        android:text="空气质量"
        android:textColor="#fff"
        android:textSize="20sp"/>
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="15dp">
        <RelativeLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1">
            <LinearLayout
                android:orientation="vertical"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_centerInParent="true">
                <TextView
                    android:id="@+id/aqi_text"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:textColor="#fff"
                    android:textSize="40sp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:textColor="#fff"
                    android:text="AQI指数"/>
            </LinearLayout>
        </RelativeLayout>
        <RelativeLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1">
            <LinearLayout
                android:orientation="vertical"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_centerInParent="true">
                <TextView
                    android:id="@+id/pm25_text"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:textColor="#fff"
                    android:textSize="40sp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:text="PM2.5指数"
                    android:textColor="#fff"/>
            </LinearLayout>
        </RelativeLayout>
    </LinearLayout>

</LinearLayout>
```

使用半透明背景，最上方定义了“空气质量”标题，然后实现了左右平分且居中对齐的布局，分别用于显示AQI指数和PM2.5指数。

#### suggestion.cml作为生活建议信息的布局



```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="15sp"
    android:background="#8000">
    <TextView
        android:layout_marginLeft="15dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="15dp"
        android:text="生活建议"
        android:textColor="#fff"
        android:textSize="20sp"/>
    <TextView
        android:id="@+id/comfort_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="15dp"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/car_wash_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="15dp"
        android:textColor="#fff"/>
    <TextView
        android:id="@+id/sport_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="15dp"
        android:textColor="#fff"/>

</LinearLayout>
```

同样使用半透明背景和一个标题，用三个textView显示舒适度、洗车指数和运动建议的相关数据。

#### 将以上布局文件引入activity_weather.xml中



```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_weather"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/colorPrimary"
    tools:context="com.example.stardream.coolweather.activity.WeatherActivity">
    <ScrollView
        android:id="@+id/weahter_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:scrollbars="none"
        android:overScrollMode="never">
        <LinearLayout
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
            <include layout="@layout/title"/>
            <include layout="layout/now"/>
            <include layout="@layout/forecast"/>
            <include layout="@layout/aqi"/>
            <include layout="@layout/suggestion"/>
        </LinearLayout>
    </ScrollView>

</FrameLayout>
```

最外层布局使用了FrameLayout，然后嵌套了一个ScrollView，可以滚动查看屏幕之外的内容。因为**ScrollView内部只允许存在一个子布局**，因此嵌入垂直方向的Lin，将其余所有布局引入。

### 将天气显示在界面

#### 解析天气JSON数据



```java
    //将返回的JSON数据解析成Weather实体类
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeahter");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
```

在Utility中添加解析JSON数据的方法，handleWeatherResponse（）方法中先通过JSONObject和JSONArray将天气中的主体内容解析出来，之后可通过调用dromJson()方法将JSON转换成Weather对象。

#### 编写WeatherActivity()中的代码

##### 定义控件的变量



```java
public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
……
}
```

##### onCreate()方法

获取控件的实例，从本地缓存读取天气数据，若没有，则会从Intent中读出天气Id，然后调用requestWeahter()方法请求服务器上的数据。
 **注意：**这里缓存中保存数据采用SharedPreferences的方式，具体用法见 [SharedPreferences存储](https://www.jianshu.com/p/65b6369d020c)



```tsx
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化控件
        weatherLayout = (ScrollView)findViewById(R.id.weahter_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            //若有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }
```

#### 向服务器请求天气信息



```java
   /*
    * 根据天气的Id向服务器请求天气信息*/
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+
                weatherId+"&key=";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    public void run(){
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather !=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
```

requestWeather()方法先使用传入的天气id和APIKEY拼装出接口地址，然后调用HttpUtil.sendOkHttpRequest()方法向该地址发送请求，服务器会以JSON格式返回天气信息。然后在onResponse()回调中调用Utility.handleWeatherResponse()将返回的JSON数据转换成Weather对象，将线程切换到主线程。若服务器返回的status状态是ok，则将返回数据缓存到SharedPreferences中，并进行显示。

#### showWeatherInfo()显示天气信息



```tsx
/*处理冰战士Weather实体类中的数据
    * */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecas_item,forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carwash = "洗车指数："+weather.suggestion.carWash.info;
        String sport = "运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carwash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
```

从Weather对象获取数据，然后显示到相应的空间上。

#### 从县列表跳转到天气界面



```dart
public class ChooseAreaFragment extends Fragment {
……
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
                }else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();

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
……
}
  在点击县级列表之后，将选中的县的天气id传递出去，启动WeatherActivity。
#### 修改MainActivity
  若在缓存中存在天气信息时，不再进行选择而是直接跳转到天气界面。
```

package com.example.stardream.coolweather.activity;

import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.support.v7.app.AppCompatActivity;
 import android.os.Bundle;

import com.example.stardream.coolweather.R;

public class MainActivity extends AppCompatActivity {



```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if(prefs.getString("weather",null) != null){
        Intent intent = new Intent(this,WeatherActivity.class);
        startActivity(intent);
        finish();
    }
}
```

}



```bash
###获取每日一图改变天气背景
####修改activity_weather.xml中的代码
```

<?xml version="1.0" encoding="utf-8"?>
 <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
 xmlns:tools="http://schemas.android.com/tools"
 android:id="@+id/activity_weather"
 android:layout_width="match_parent"
 android:layout_height="match_parent"
 android:background="@color/colorPrimary"
 tools:context="com.example.stardream.coolweather.activity.WeatherActivity">



```xml
<ImageView
    android:id="@+id/bing_pic_img"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scaleType="centerCrop"/>

<ScrollView
    android:id="@+id/weahter_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:scrollbars="none"
    android:overScrollMode="never">
    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <include layout="@layout/title"/>
        <include layout="layout/now"/>
        <include layout="@layout/forecast"/>
        <include layout="@layout/aqi"/>
        <include layout="@layout/suggestion"/>
    </LinearLayout>
</ScrollView>
```

</FrameLayout>



```bash
  增加ImageView作为背景图片。
####修改WeatherActivity中的代码
##### ImageView控件的定义
```

private ImageView bingPicImg;



```bash
##### 在onCreate()中
```

bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
 String bingPic = prefs.getString("bing_pic",null);
 if(bingPic !=null){
 Glide.with(this).load(bingPic).into(bingPicImg);
 }else{
 loadBingPic();
 }
 ···
 若缓存中有图片，则直接调用Glide方式取出，若没有，向服务器请求。

##### loadBingPic()方法



```java
    /*
    * 加载必应图片，每日一图*/
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg)
                    }
                });

            }
        });
    }
```

##### 在requestWeather()方法中



```undefined
loadBingPic();
```

每次请求天气信息时也会刷新背景图片

##### 解决背景图片和状态栏没有融合的问题

在onCreate()方法中



```cpp
 //Android5.0及以上系统才支持，即版本号大于等于21
        if(Build.VERSION.SDK_INT>=21){
            //调用getWindow().getDecorView()拿到当前活动的DecorView
            View decorView = getWindow().getDecorView();
            //改变系统的UI显示，传入的两个值表示活动的布局会显示在状态栏上面
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //将状态栏设置成透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
```

头布局和系统状态栏紧贴到了一起，修改activity_weather.xml中的代码，在ScrollView的LinearLayout中增加了android:fitsSystemWindows属性，设置成true表示会为系统状态栏留出空间。



```xml
<LinearLayout
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:fitsSystemWindows="true">
            <include layout="@layout/title"/>
            <include layout="layout/now"/>
            <include layout="@layout/forecast"/>
            <include layout="@layout/aqi"/>
            <include layout="@layout/suggestion"/>
        </LinearLayout>
```

# 手动更新天气和切换城市

## 手动更新天气

采用下拉刷新方式手动更新天气

#### 修改activity_weather.xml



```jsx
<!--SwipeRefreshLayout具有下拉刷新功能-->
    <android.support.v4.widget.SwipeRefreshLayout
        android:id="@+id/swipe_refresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    <ScrollView
        ……
    </ScrollView>
    </android.support.v4.widget.SwipeRefreshLayout>
```

#### 修改WeatherActivity中的代码

##### 定义刷新控件



```cpp
private SwipeRefreshLayout swipeRefresh;
```

##### 加载控件，设置下拉进度条颜色



```dart
swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
//定义一个weatherId
        final String weatherId;
```

##### 实现更新



```csharp
    if(weatherString != null){
            //若有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            //若有缓存得到weatherId
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            public void onRefresh(){
                requestWeather(weatherId);
            }
        });
```

##### 隐藏刷新进度条



```java
 /*
    * 根据天气的Id向服务器请求天气信息*/
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+
                weatherId+"&key=";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    public void run(){
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        //刷新事件结束，隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather !=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        //刷新事件结束，隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });

            }
        });
        loadBingPic();//每次请求天气信息时会刷新背景图片
    }
```

## 切换城市

#### 在title.xml标题栏设置按钮



```objectivec
<Button
        android:id="@+id/nav_button"
        android:layout_width="30dp"
        android:layout_height="30dp"
        android:layout_marginLeft="10dp"
        android:layout_alignParentLeft="true"
        android:layout_centerVertical="true"
        android:background="@drawable/ic_home"/>
```

#### 修改activity_weather.xml布局加入滑动菜单功能



```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_weather"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/colorPrimary"
    tools:context="com.example.stardream.coolweather.activity.WeatherActivity">

    <ImageView
        android:id="@+id/bing_pic_img"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"/>
    <!--SwipeRefreshLayout具有下拉刷新功能-->
    <!--fitsSystemWindows为系统状态栏留出空间-->
    <android.support.v4.widget.DrawerLayout
        android:id="@+id/drawer_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        >

    <android.support.v4.widget.SwipeRefreshLayout
        android:id="@+id/swipe_refresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    <ScrollView
        android:id="@+id/weahter_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:scrollbars="none"
        android:overScrollMode="never">
        <LinearLayout
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:fitsSystemWindows="true">
            <include layout="@layout/title"/>
            <include layout="layout/now"/>
            <include layout="@layout/forecast"/>
            <include layout="@layout/aqi"/>
            <include layout="@layout/suggestion"/>
        </LinearLayout>
    </ScrollView>
    </android.support.v4.widget.SwipeRefreshLayout>
    <fragment
        android:id="@+id/choose_area_fragment"
        android:name="com.example.stardream.coolweather.activity.ChooseAreaFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="start"/>
    </android.support.v4.widget.DrawerLayout>

</FrameLayout>
```

DrawerLayout中的第一个子控件用于作为主屏幕中显示的内容；
 第二个子控件用于作为滑动菜单中显示的内容，添加了用于遍历省市县数据的碎片。

### 修改WeatherActivity中的代码加入滑动菜单的逻辑控制

##### 定义Button和DrawerLayout



```cpp
public DrawerLayout drawerLayout;
    private Button navButton;
```

##### onCreate()



```csharp
drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
```

在onCreate()中获取新增的DrawerLayout和Button的实例，然后在Button点击事件中调用DrawerLayout的openDrawer()方法打开活动菜单即可。

#### 请求新选择的城市的天气信息

因为原来的跳转是从MainActivity中跳转过去的，现在就在WeatherActivity中，所以就关闭滑动菜单，显示下拉刷新进度条，请求新城市的天气信息。
 在ChooseAreaFragment中的onActivityCreated()中，



```java
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
                }else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity()instanceof MainActivity){
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }


                }
            }
        });
```

# 后台自动更新天气

要想自动更新天气，需要创建一个长期在后台运行额定时任务，因此新建一个服务AutoUpdateService。

#### onStartCommand()方法



```java
package com.example.stardream.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.stardream.coolweather.gson.Weather;
import com.example.stardream.coolweather.util.HttpUtil;
import com.example.stardream.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*1000;//8个小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }
```

在onStartCommand()方法中先调用了updateWeather()方法更新天气，调用updateBingPic()方法更新背景图片，将更新时间设置为8小时，定时闹钟见[AlarmManager用法](https://www.jianshu.com/p/e0d6fddea66e)

#### updateWeather()方法



```java
  /*更新天气信息
    * */
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString !=null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;

            String weatherUrl = "http://guolin.tech/api/weather?cityid="+
                    weatherId+"&key=";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if(weather !=null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }

                }
            });

        }
    }
```

#### updateBingPic()方法



```java
 /*更新必应每日一图
    * */
    private void updateBingPic(){
        String requestBingPic ="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }
```

#### 修改WeatherActivity

在showWeather()方法的最后加入启动AutoUpdateService这个服务的代码，一旦选中某个城市并成功更新天气之后，AutoUpdateService就会一直在后台运行，并保证每8小时更新一次天气。



```java
private void showWeatherInfo(Weather weather){
        ……
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        }
       ……
    }
```

# 修改图标和名称

## 在AndroidManifest.xml修改图标



```bash
android:icon="@mipmap/logo"
```

## 在strings.xml修改app名称



```cpp
<string name="app_name" translatable="false">我的天气</string>
```

------

终于学（照猫画虎）完了这个天气预报，可是还没有正常运行，据说是和风天气接口过期了，汗颜。可能还要重新找新的API调用。

------

emmmmm……我来为其正名，接口没问题，是自己的原因，天气数据是可以成功调出来的，现在界面处理方面还有点问题，debug中……

------

我发现这本书的代码还是有问题的，如下：

- 1.ListView与ArrayAdapter的使用不当，每当适配器中的内容发生变化时，要再一次载入listView。



```cpp
adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
            //载入listView
            listView.setAdapter(adapter);
```

- 2.每一次在数据库中查询是否存在省市县以及天气的数据，若存在则直接取出显示出来。这个完全没有考虑存在于数据库中的数据是否是选中的省市县，不然会导致选中的省市县和显示出来的不相符，因此要添加限制条件。整个这一块的代码都很混乱。。。在经历心情第一丧之后终于调出来了，细节修改的地方忘记了，核心在这里。

###### queryCities()



```kotlin
//在数据库中查询对应的City数据
        //原来代码的问题时把所有City的数据取出来了，然后就发生混乱
        //应该取出的是选中省份的city
        cityList = DataSupport.where("provinceId = ?",String.valueOf(selectedProvince.getId())).find(City.class);
```

###### queryCounties()



```kotlin
//在数据库中查询对应的county数据
        //原来代码的问题时把所有County的数据取出来了，然后就发生混乱
        //应该取出的是选中city的county
        countyList = DataSupport.where("cityId = ?",String.valueOf(selectedCity.getId())).find(County.class);
```

------

# 界面展示

![img](https:////upload-images.jianshu.io/upload_images/13122291-bd05e2306d3cddf5.png?imageMogr2/auto-orient/strip|imageView2/2/w/205/format/webp)

Icon



![img](https:////upload-images.jianshu.io/upload_images/13122291-58a5d63b107862c0.png?imageMogr2/auto-orient/strip|imageView2/2/w/437/format/webp)

主界面



![img](https:////upload-images.jianshu.io/upload_images/13122291-6023032bbc5b1f25.png?imageMogr2/auto-orient/strip|imageView2/2/w/437/format/webp)

省级—甘肃



![img](https:////upload-images.jianshu.io/upload_images/13122291-a5ac742342e723b3.png?imageMogr2/auto-orient/strip|imageView2/2/w/435/format/webp)

市级—临夏

最关键的界面图片总是上传失败，不知道是什么原因。。。不传了，真是想吐槽这个上传图片功能



作者：Aptitude
链接：https://www.jianshu.com/p/233d220e80b0
来源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。