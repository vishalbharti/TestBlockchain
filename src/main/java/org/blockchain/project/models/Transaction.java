package org.blockchain.project.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class Transaction {

    private String sender;
    private String recipient;
    private String data;
    private String timestamp;
    private String txHash;
    private String txSignature;
    private Boolean isValid;
    
    public Transaction() {}
    
    public Transaction(String sender, String recipient, String data, String timestamp, String txHash, String txSignature, Boolean isValid) {
    	this.sender = sender;
    	this.recipient = recipient;
    	this.data = data;
    	this.timestamp = timestamp;
    	this.txHash = txHash;
    	this.txSignature = txSignature;
    	this.isValid = isValid;
    }
    
    public Transaction(Transaction transaction) {
    	this(transaction.sender, transaction.recipient, transaction.data, transaction.timestamp, transaction.txHash, transaction.txSignature, transaction.isValid);
    }
    
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getRecipient() {
        return recipient;
    }
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getTxHash() {
		return txHash;
	}
	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}
    
	public String getTxSignature() {
        return txSignature;
    }
    public void setTxSignature(String txSignature) {
        this.txSignature = txSignature;
    }
    
    public Boolean isValid() {
		return isValid;
	}
	public void setValid(Boolean isValid) {
		this.isValid = isValid;
	}
	
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sender", getSender());
        jsonObject.put("recipient", getRecipient());
        jsonObject.put("data", getData());
        jsonObject.put("timestamp", getTimestamp());
        jsonObject.put("txHash", getTxHash());
        jsonObject.put("txSignature", getTxSignature());
        jsonObject.put("isValid", isValid());
        
        return jsonObject;
    }
    
    public static Transaction init(JSONObject transactionJSONObj) {
    	Transaction transaction = new Transaction();
		transaction.setSender(transactionJSONObj.has("sender") ? transactionJSONObj.getString("sender") : null);
		transaction.setRecipient(transactionJSONObj.has("recipient") ? transactionJSONObj.getString("recipient") : null);
		transaction.setData(transactionJSONObj.has("data") ? transactionJSONObj.getString("data") : null);
		transaction.setTimestamp(transactionJSONObj.has("timestamp") ? transactionJSONObj.getString("timestamp") : null);
		transaction.setTxHash(transactionJSONObj.has("txHash") ? transactionJSONObj.getString("txHash") : null);
		transaction.setTxSignature(transactionJSONObj.has("txSignature") ? transactionJSONObj.getString("txSignature") : null);
		transaction.setValid(transactionJSONObj.has("isValid") ? transactionJSONObj.getBoolean("isValid") : null);
		
		return transaction;
    }
    
    public String toString() {
    	return this.getSender().toString() + this.getRecipient().toString()
    				+ this.getData().toString() + (this.getTimestamp() != null ? this.getTimestamp().toString(): "");
    }
}
