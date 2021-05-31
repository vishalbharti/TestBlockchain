package org.blockchain.project.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Block {

    private List<Transaction> transactions;
    private String merkelHash;
    private int height;
    private int nonce;
    private String timestamp;
    private String previousBlock;
    private String hash;
    private Boolean isValid;
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getNonce() {
        return nonce;
    }
    public void setNonce(int nonce) {
        this.nonce = nonce;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPreviousBlock() {
        return previousBlock;
    }
    public void setPreviousBlock(String previousBlock) {
        this.previousBlock = previousBlock;
    }
    
    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public String getMerkelHash() {
		return merkelHash;
	}
	public void setMerkelHash(String merkleHash) {
		this.merkelHash = merkleHash;
	}
    
	public Boolean isValid() {
		return isValid;
	}
	public void setValid(Boolean isValid) {
		this.isValid = isValid;
	}
	
    public JSONObject toJSONObject() throws JSONException {
    	JSONObject block = new JSONObject();
    	
    	JSONArray transactions = new JSONArray();
    	if(getTransactions() != null) {
        	for(Transaction transaction: getTransactions()) {
        		transactions.put(transaction.toJSONObject());
        	}
    	}
    	block.put("transactions", transactions.length() == 0? null: transactions);
    	block.put("merkelHash", getMerkelHash());
    	block.put("height", getHeight());
    	block.put("nonce", getNonce());
    	block.put("timestamp", getTimestamp());
    	block.put("previousBlock", getPreviousBlock());
    	block.put("hash", getHash());
    	
    	return block;
    }
    
    public static Block init(JSONObject blockJSONObject) {
    	Block block = new Block();
    	if(blockJSONObject.has("transactions")) {
    		List<Transaction> transactions = new ArrayList<>();
    		JSONArray transactionsJSONArray = blockJSONObject.getJSONArray("transactions");
    		for(int i = 0; i < transactionsJSONArray.length(); i++) {
    			transactions.add(Transaction.init(transactionsJSONArray.getJSONObject(i)));
    		}
    		block.setTransactions(transactions);
    	}
    	block.setMerkelHash(blockJSONObject.has("merkelHash") ? blockJSONObject.getString("merkelHash") : null);
    	block.setHeight(blockJSONObject.has("height") ? blockJSONObject.getInt("height") : null);
    	block.setNonce(blockJSONObject.has("nonce") ? blockJSONObject.getInt("nonce") : null);
    	block.setTimestamp(blockJSONObject.has("timestamp") ? blockJSONObject.getString("timestamp") : null);
    	block.setPreviousBlock(blockJSONObject.has("previousBlock") ? blockJSONObject.getString("previousBlock") : null);
    	block.setHash(blockJSONObject.has("hash") ? blockJSONObject.getString("hash") : null);
    	
    	return block;
    }
}
