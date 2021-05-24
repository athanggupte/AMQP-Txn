package com.xdevapps;

import java.util.Date;

public class TxnRequestPayload {
    
    private String txnId;
    private Date   timestamp;
    private int    type;
    private int    amount;
    
    public TxnRequestPayload(String txnId, Date timestamp, int type, int amount) {
        this.txnId = txnId;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
    }

    public String getTxnId() {
        return txnId;
    }
    
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }



}
