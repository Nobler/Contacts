package com.wdjhzw.contacts.contact;

import java.util.List;

public class RawContact {

    private long id;
    private long contacId;
    private String accountType;
    private String accountName;

    private List<Data> dataList;

    public RawContact(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public RawContact setId(long id) {
        this.id = id;

        return this;
    }

    public long getContacId() {
        return contacId;
    }

    public RawContact setContacId(long contacId) {
        this.contacId = contacId;

        return this;
    }

    public String getAccountType() {
        return accountType;
    }

    public RawContact setAccountType(String accountType) {
        this.accountType = accountType;

        return this;
    }

    public String getAccountName() {
        return accountName;
    }

    public RawContact setAccountName(String accountName) {
        this.accountName = accountName;

        return this;
    }
    
    public List<Data> getDataList() {
        return dataList;
    }
    
    public RawContact setDataList(List<Data> data) {
        this.dataList = data;
        
        return this;
    }

    @Override
    public String toString() {
        return "RawContact [id=" + id + ", contacId=" + contacId
                + ", accountType=" + accountType + ", accountName="
                + accountName + ", data=" + dataList + "]";
    }

}
