package org.blockchain.project.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.blockchain.project.models.Block;
import org.blockchain.project.models.Blockchain;
import org.blockchain.project.models.Transaction;
import org.blockchain.project.models.Wallet;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class UtilImpl implements Util {
	
    private Environment environment;
    private Blockchain blockchain;
	
    @Autowired
	public UtilImpl(Environment environment, Blockchain blockchain) {
		this.environment = environment;
		this.blockchain = blockchain;
	}
    
    @Override
	public String createSHA256(String textToHash) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return Base64.toBase64String(messageDigest.digest(textToHash.getBytes(StandardCharsets.UTF_8)));
    }
    
    @Override
    public Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    } 
    
    @Override
    public String readFile(String property) throws IOException {
        Path filePath = Paths.get(environment.getProperty(property));
        
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {        
            for(String line: (Iterable<String>) lines::iterator) {
                return line;
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    @Override
    public void appendDataToFile(byte[] content, String property) throws IOException {
        Path filePath = Paths.get(environment.getProperty(property));
        Files.write(filePath, content, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    @Override
    public Block adjustNonce(Block block) throws NoSuchAlgorithmException {
    	 while(!hashedBlock(block).getHash().startsWith(blockchain.getDifficulty())) {
    		 block.setNonce(block.getNonce() + 1);
    	 }
    	 return block;
    }
    
    @Override
    public Wallet createWallet() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA","BC");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("prime192v1");
        
        keyPairGenerator.initialize(ecGenParameterSpec, secureRandom);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        
        Wallet wallet = new Wallet();
        
        wallet.setPrivateKey(Base64.toBase64String(new PKCS8EncodedKeySpec(privateKey.getEncoded()).getEncoded()));
        wallet.setPublicKey(Base64.toBase64String(new X509EncodedKeySpec(publicKey.getEncoded()).getEncoded()));
        
        return wallet;
    }
    
    @Override
    public String signDataViaPrivateKey(PrivateKey privateKey, String data) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("ECDSA", "BC");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        
        return Base64.toBase64String(signature.sign());
    }
    
    @Override
    public boolean verifySignedDataViaPublicKey(PublicKey publicKey, String data, String signedData) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("ECDSA", "BC");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        return signature.verify(DatatypeConverter.parseBase64Binary(signedData));
    }
    
    @Override
    public String generateMerkelTree(List<Transaction> transactions) throws NoSuchAlgorithmException {
    	if(transactions != null) {
	    	List<String> hashedTransactionList = createHashedTransactionList(transactions);
	    	return merkelTree(hashedTransactionList);
    	} else {
    		return "";
    	}
    }
    
    private Block hashedBlock(Block block) throws NoSuchAlgorithmException {
    	block.setHash(this.createSHA256(block.getMerkelHash() + block.getHeight() + block.getNonce() +
    			block.getTimestamp() + block.getPreviousBlock()));
    	return block;
    }
    
    private String merkelTree(List<String> transactions) throws NoSuchAlgorithmException {
    	List<String> compressedHashedTransactionList = new ArrayList<>();
    	
    	int numberOfTransactions = transactions.size();
		if(numberOfTransactions % 2 != 0) {
			transactions.add(transactions.get(numberOfTransactions - 1));
		}
		for(int i = 0; i < transactions.size(); i+=2) {
			compressedHashedTransactionList.add(this.createSHA256(transactions.get(i) + transactions.get(i + 1)));
		}
		
		if(compressedHashedTransactionList.size() != 1) {
			return merkelTree(compressedHashedTransactionList);
		} else {
			return compressedHashedTransactionList.get(0);
		}
    }
    
    private List<String> createHashedTransactionList(List<Transaction> transactions) throws NoSuchAlgorithmException {
    	List<String> hashedTransactionList = new ArrayList<>();

    	for(Transaction transaction: transactions) {
    		hashedTransactionList.add(transaction.getTxHash());
    	}
    	
    	return hashedTransactionList;
    }
}
