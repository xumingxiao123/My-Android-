package com.example.sunnywheather.db;

import org.litepal.crud.LitePalSupport;
/**
 * Created by StarDream on 2020/8/21.
 */
public class City extends LitePalSupport {
    private int id;
    private String cityName;
    private int cityCode;
    private int provinceId;  //城市ID

    public int getId() {
        return id; }

    public void setId(int id) {
        this.id = id; }

    public String getCityName() {
        return cityName; }

    public void setCityName(String cityName) {
        this.cityName = cityName; }

    public int getCityCode() {
        return cityCode; }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode; }

    public int getProvinceId() {
        return provinceId; }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId; }
}
