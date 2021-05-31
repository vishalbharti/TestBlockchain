package org.blockchain.project.controllers;

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
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.blockchain.project.models.Block;
import org.blockchain.project.models.Blockchain;
import org.blockchain.project.models.Transaction;
import org.blockchain.project.models.Wallet;
import org.blockchain.project.services.BlockchainService;
import org.blockchain.project.services.Util;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;
    
    @Autowired
    private Blockchain blockchain;
    
    @Autowired
    private Transaction transaction;
    
    @Autowired
    private Wallet wallet;
    
    @Autowired
    private Util util;
    
    @RequestMapping(value="/blockchain", method=RequestMethod.GET)
    public List<Block> displayBlockchain() throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, InvalidKeySpecException {
        return blockchainService.displayBlockchain();
    }
    
    @RequestMapping(value="/addTransaction", method=RequestMethod.POST)
    public ResponseEntity<?> addTransaction(@RequestBody Transaction transaction) throws JSONException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, InvalidKeySpecException {
    	blockchainService.addTransaction(transaction);
    	
    	JSONObject successMessage = new JSONObject();
        successMessage.put("message", "Transaction added to open transactions list");
        
        return new ResponseEntity<>(successMessage.toMap(), HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/mine", method=RequestMethod.POST)
    public ResponseEntity<?> addTransactionsToBlockchain() throws JSONException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, InvalidKeySpecException {
    	blockchain.setDifficulty("00");
        blockchainService.addTransactionsToBlockchain();
        
        JSONObject successMessage = new JSONObject();
        successMessage.put("message", "Transactions added to blockchain");
        
        return new ResponseEntity<>(successMessage.toMap(), HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/createWallet", method=RequestMethod.POST)
    public Wallet createWallet() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException, JSONException {
        return blockchainService.createWallet();
    }
    
    @RequestMapping(value="/loadWallet", method=RequestMethod.POST)
    public ResponseEntity<?> loadWallet(@RequestBody Wallet wallet) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeySpecException {
        // Test to match Public-Private Key values
    	KeyFactory keyFactory = KeyFactory.getInstance("ECDSA","BC");
    	String data = "TestData";
    	String signedData = util.signDataViaPrivateKey(keyFactory.generatePrivate(new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(wallet.getPrivateKey()))), data);
        if(!util.verifySignedDataViaPublicKey(keyFactory.generatePublic(new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(wallet.getPublicKey()))), data, signedData)) {
        	JSONObject errorMessage = new JSONObject();
        	errorMessage.put("message", "Access Denied: Public-Private value pair didn't match");
        	return new ResponseEntity<>(errorMessage.toMap(), HttpStatus.FORBIDDEN);
        }
    	
        // Stores Key into node
        transaction.setSender(wallet.getPublicKey());
        this.wallet.setPublicKey(wallet.getPublicKey());
        this.wallet.setPrivateKey(wallet.getPrivateKey());
        
        return new ResponseEntity<>(blockchainService.getAllData(wallet.getPublicKey()), HttpStatus.ACCEPTED);
    }
}