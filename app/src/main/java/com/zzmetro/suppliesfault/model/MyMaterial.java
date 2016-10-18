package com.zzmetro.suppliesfault.model;

/**
 * Created by mayunpeng on 16/7/28.
 */
public class MyMaterial {
    // 备件ID
    private String materialID;
    // 物资简介
    private String materialName;
    // 持有量
    private String materialAmount;

    public String getMaterialID() {
        return materialID;
    }

    public void setMaterialID(String materialID) {
        this.materialID = materialID;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialAmount() {
        return materialAmount;
    }

    public void setMaterialAmount(String materialAmount) {
        this.materialAmount = materialAmount;
    }

    @Override
    public String toString() {
        return "materialID:" + materialID + ",materialName:" + materialName + ",materialAmount:" + materialAmount;
    }
}
