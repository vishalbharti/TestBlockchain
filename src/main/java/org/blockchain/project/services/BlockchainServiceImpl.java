package org.blockchain.project.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.blockchain.project.models.Block;
import org.blockchain.project.models.Blockchain;
import org.blockchain.project.models.Transaction;
import org.blockchain.project.models.Wallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlockchainServiceImpl implements BlockchainService {

    @Autowired
    private Blockchain blockchain;
    
    @Autowired
    private Wallet wallet;
    
    @Autowired
    private Util util;
    
    @Override
    public List<Block> displayBlockchain() throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, InvalidKeySpecException {
        String blockChainStringRep = util.readFile("blockchain.file");
        
        if(blockChainStringRep!= null) {
        	List<Block> blocks = new ArrayList<>();
        	JSONArray blockChainJSONArray = new JSONArray(blockChainStringRep);
        	
        	for(int i = 0; i < blockChainJSONArray.length(); i++) {
        		blocks.add(Block.init(blockChainJSONArray.getJSONObject(i)));
        	}
        	
        	for(int i = 0; i < blocks.size(); i++) {
        		Block block = blocks.get(i);
        		
        		boolean areAllTransactionsValid = true;
        		if(block.getTransactions() != null) {
        			List<Transaction> txList = block.getTransactions();
	        		for(int j = 0; j < txList.size(); j++) {
	        			txList.get(j).setValid(verifyTransaction(txList.get(j)));
	        			
	        			if(areAllTransactionsValid) {
	        				areAllTransactionsValid = txList.get(j).isValid();
	        			}
	        		}
        		}
        		
        		if(!areAllTransactionsValid) {
        			block.setValid(false);
        			continue;
        		}
        		
        		if(i > 0 && !blocks.get(i - 1).isValid()) {
        			block.setValid(false);
        			continue;
        		}
        		block.setValid(verifyBlock(block));
        	}
        	
        	return blocks;
        }
        
        return null;
    }
    
    @Override
	public void addTransaction(Transaction transaction) throws JSONException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, InvalidKeySpecException {
    	// Sign transaction
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA","BC");
        
        transaction.setSender(wallet.getPublicKey());
        String dataToEncrypt = transaction.getSender() + transaction.getRecipient() + transaction.getData();
        
    	transaction.setTimestamp(util.getCurrentTimestamp().toString());
    	transaction.setTxHash(util.createSHA256(transaction.toString()));
    	transaction.setTxSignature(util.signDataViaPrivateKey(keyFactory.generatePrivate(new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(wallet.getPrivateKey()))), dataToEncrypt));
    	blockchain.getOpenTransactions().add(transaction);
    	
    	JSONArray openTransactionsArray = new JSONArray();
    	for(Transaction t : blockchain.getOpenTransactions()) {
    		openTransactionsArray.put(t.toJSONObject());
    	}
    	util.appendDataToFile(openTransactionsArray.toString().getBytes(), "open.transactions.file");
	}
    
    @Override
    public void addTransactionsToBlockchain() throws NoSuchAlgorithmException, IOException, JSONException, NoSuchProviderException, InvalidKeyException, SignatureException, InvalidKeySpecException {
    	if(blockchain.getBlockchain() == null || blockchain.getBlockchain().size() == 0) {
    		blockchain.getBlockchain().add(createGenesisBlock());
    	}
    	
    	// Verify Transactions
    	Iterator<Transaction> txIterator =  blockchain.getOpenTransactions().iterator();
    	while(txIterator.hasNext()) {
    	    Transaction transaction = txIterator.next();
    	    
    	    if(!verifyTransaction(transaction)) {
    	        blockchain.getOpenTransactions().remove(transaction);
    	    }
    	}
    	
    	Block previousBlock = blockchain.getBlockchain().get(blockchain.getBlockchain().size() - 1);
    	
    	Block block = new Block();
    	List<Transaction> transactions = new ArrayList<>();
    	for(Transaction transaction : blockchain.getOpenTransactions()) {
    		transactions.add(new Transaction(transaction));
    	}
    	
    	block.setTransactions(transactions);
    	block.setPreviousBlock(previousBlock.getHash());
    	block.setHeight(previousBlock.getHeight() + 1);
    	block.setTimestamp(util.getCurrentTimestamp().toString());
    	block.setMerkelHash(util.generateMerkelTree(block.getTransactions()));
    	util.adjustNonce(block);
    	
    	blockchain.getBlockchain().add(block);
    	
    	//Save blockchain and update open transaction
    	JSONArray blockArray = new JSONArray();
    	for(Block updatedBlock: blockchain.getBlockchain()) {
    		blockArray.put(updatedBlock.toJSONObject());
    	}
    	
    	util.appendDataToFile(blockArray.toString().getBytes(), "blockchain.file");
    	
    	// Clears open Transactions
    	blockchain.getOpenTransactions().clear();
    	util.appendDataToFile("".toString().getBytes(), "open.transactions.file");
    } 
    
    @Override
    public void populateOpenTransactions(JSONArray jsonArray) {
    	List<Transaction> openTransactions = blockchain.getOpenTransactions();
    	
    	for(int i = 0; i < jsonArray.length(); i++) {
    		JSONObject transactionJSONObj = jsonArray.getJSONObject(i);
    		openTransactions.add(Transaction.init(transactionJSONObj));
    	}
    }
    
    @Override
    public void populateBlockchain(JSONArray blockChainJSONArray) {
        List<Block> blocks = blockchain.getBlockchain();
        
        for(int i = 0; i < blockChainJSONArray.length(); i++) {
            blocks.add(Block.init(blockChainJSONArray.getJSONObject(i)));
        }
    }
    
    @Override
    public Wallet createWallet() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        return util.createWallet();
    }
    
    @Override
    public List<Transaction> getAllData(String publicKey) {
    	List<Transaction> userOwnedTransactions = new ArrayList<>();
    	
    	for(Block block: this.blockchain.getBlockchain()) {
    	    if(block.getTransactions() != null) {
        		for(Transaction transaction: block.getTransactions()) {
        			if(transaction.getSender().equals(publicKey)) {
        				userOwnedTransactions.add(transaction);
        			}
        		}
    	    }
    	}
    	
    	return userOwnedTransactions;
    }
    
    @Override
    public boolean verifyTransaction(Transaction transaction) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeySpecException {
    	boolean isTransactionValid = false;
    	KeyFactory keyFactory = KeyFactory.getInstance("ECDSA","BC");
    	
    	String data = transaction.getSender() + transaction.getRecipient() + transaction.getData();
	    if(util.verifySignedDataViaPublicKey(keyFactory.generatePublic(new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(transaction.getSender()))), data, transaction.getTxSignature())) {
	    	isTransactionValid = true;
	    }
	    
	    return isTransactionValid;
    }
    
    @Override
    public boolean verifyBlock(Block block) throws NoSuchAlgorithmException {
    	boolean isBlockValid = true;
    	
    	// Validates hash as per difficulty
//    	if(!block.getHash().startsWith(this.blockchain.getDifficulty())) {
//    		isBlockValid = false;
//    	}
    	// difficulty may vary as per time, thus commenting out above LOC
    	
    	if(!util.createSHA256(block.getMerkelHash() + block.getHeight() + block.getNonce() + block.getTimestamp() + block.getPreviousBlock()).equals(block.getHash())) {
    		isBlockValid = false;
    	}
    	
    	return isBlockValid;
    }
    
    private Block createGenesisBlock() throws NoSuchAlgorithmException {
        Block genesisBlock = new Block();
        genesisBlock.setTransactions(null);
        genesisBlock.setPreviousBlock("00000000000000000000000000000000000000000000");
        genesisBlock.setTimestamp(util.getCurrentTimestamp().toString());
        genesisBlock.setHeight(0);
        util.adjustNonce(genesisBlock);
        
        return genesisBlock;
    }
}
