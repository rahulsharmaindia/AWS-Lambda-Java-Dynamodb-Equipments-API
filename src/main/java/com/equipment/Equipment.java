package com.equipment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

public class Equipment {

    private long equipment_number;
    private String address;
    private Date contract_start_date;
    private Date contract_end_date;
    private String status;

    public Equipment(){

    }
    public Equipment(String json) {
        this();
        Gson gson = new Gson();
        Equipment request = gson.fromJson(json, Equipment.class);
        this.equipment_number = request.getEquipment_number();
        this.address = request.getAddress();
        this.contract_start_date = request.getContract_start_date();
        this.contract_end_date = request.getContract_end_date();
        this.status = request.getStatus();
    }

    public long getEquipment_number() {
        return equipment_number;
    }

    public void setEquipment_number(long equipment_number) {
        this.equipment_number = equipment_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getContract_start_date() {
        return contract_start_date;
    }

    public void setContract_start_date(Date contract_start_date) {
        this.contract_start_date = contract_start_date;
    }

    public Date getContract_end_date() {
        return contract_end_date;
    }

    public void setContract_end_date(Date contract_end_date) {
        this.contract_end_date = contract_end_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}