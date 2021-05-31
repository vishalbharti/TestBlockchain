package org.blockchain.project.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.blockchain.project.models.Block;
import org.blockchain.project.models.Transaction;
import org.blockchain.project.models.Wallet;
import org.json.JSONArray;
import org.json.JSONException;

public interface BlockchainService {

    public List<Block> displayBlockchain() throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, InvalidKeySpecException ;
    
    public void addTransaction(Transaction transaction) throws JSONException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, InvalidKeySpecException ;
    
    public void addTransactionsToBlockchain() throws NoSuchAlgorithmException, IOException, JSONException, NoSuchProviderException, InvalidKeyException, SignatureException, InvalidKeySpecException ;
    
    public void populateOpenTransactions(JSONArray jsonArray);
    
    public void populateBlockchain(JSONArray blockChainJSONArray);
    
    public Wallet createWallet() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException;
    
    public List<Transaction> getAllData(String publicKey);
    
    public boolean verifyTransaction(Transaction transaction) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeySpecException ;
    
    public boolean verifyBlock(Block block) throws NoSuchAlgorithmException;
}
