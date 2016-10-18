package com.zzmetro.suppliesfault.model;

/**
 * Created by mayunpeng on 16/7/27.
 */
public class SpinnerArea {
    // 这里将domaincode作为Key，将domainname作为Value
    private String domaincode;
    private String domainname;

    public SpinnerArea() {
        this.domaincode = "";
        this.domainname = "";
    }

    public SpinnerArea(String domaincode, String domainname) {
        this.domaincode = domaincode;
        this.domainname = domainname;
    }

    public String getDomaincode() {
        return domaincode;
    }

    public void setDomaincode(String domaincode) {
        this.domaincode = domaincode;
    }

    public String getDomainname() {
        return domainname;
    }

    public void setDomainname(String domainname) {
        this.domainname = domainname;
    }

    // 将toString的返回值返回value
    @Override
    public String toString() {
        return domainname;
    }
}
