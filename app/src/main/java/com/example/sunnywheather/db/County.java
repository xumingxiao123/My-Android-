package com.example.sunnywheather.db;
import org.litepal.crud.LitePalSupport;
/**
 * Created by StarDream on 2020/8/21.
 */
public class County extends LitePalSupport {
    private int id; //市具有的id
    private String countyName; //市具有的名称
    private String weatherId; //天气的ID
    private int cityId;   //城市ID

    public int getId() {
        return id; }

    public void setId(int id) {
        this.id = id; }

    public String getCountyName() {
        return countyName; }

    public void setCountyName(String countyName) {
        this.countyName = countyName; }

    public String getWeatherId() {
        return weatherId; }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId; }

    public int getCityId() {
        return cityId; }

    public void setCityId(int cityId) {
        this.cityId = cityId; }
}