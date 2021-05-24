package com.xdevapps;

import java.util.Date;

public class TxnResponsePayload {
    
    private String  txnId;
    private Date    timestamp;
    private boolean success;
    private String  responseString;
    
    public TxnResponsePayload(String txnId, Date timestamp, boolean success, String responseString) {
        this.txnId = txnId;
        this.timestamp = timestamp;
        this.success = success;
        this.responseString = responseString;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }
    

}
