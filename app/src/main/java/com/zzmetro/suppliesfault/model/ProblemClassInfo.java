package com.zzmetro.suppliesfault.model;

/**
 * Created by mayunpeng on 16/7/27.
 */
public class ProblemClassInfo {
    //设备编号
    private String equipmentCode;
    // 故障类型
    private String problemClass;
    // 故障Code
    private String problemCode;
    // 故障描述
    private String problemComment;
    // 故障建议
    private String problemTips;

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public String getProblemClass() {
        return problemClass;
    }

    public void setProblemClass(String problemClass) {
        this.problemClass = problemClass;
    }

    public String getProblemCode() {
        return problemCode;
    }

    public void setProblemCode(String problemCode) {
        this.problemCode = problemCode;
    }

    public String getProblemComment() {
        return problemComment;
    }

    public void setProblemComment(String problemComment) {
        this.problemComment = problemComment;
    }

    public String getProblemTips() {
        return problemTips;
    }

    public void setProblemTips(String problemTips) {
        this.problemTips = problemTips;
    }

    @Override
    public String toString() {
        return "equipmentCode:" + equipmentCode + ",problemClass:" + problemClass
                + ",problemCode:" + problemCode + ",problemComment:" + problemComment
                + ",problemTips" + problemTips;
    }
}
