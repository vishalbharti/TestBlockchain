package org.blockchain.project;

import java.io.IOException;
import java.security.Security;
import org.blockchain.project.services.BlockchainService;
import org.blockchain.project.services.Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) throws BeansException, IOException {
		ConfigurableApplicationContext context = SpringApplication.run(ProjectApplication.class, args);
		
		Security.addProvider(new BouncyCastleProvider());
		// Read transactions
    	String existingOpenTransactions = context.getBean(Util.class).readFile("open.transactions.file");
    	
    	JSONArray openTransactionsArray;
    	if(existingOpenTransactions != null) {
    	    openTransactionsArray = new JSONArray(existingOpenTransactions);
    	} else {
    	    openTransactionsArray = new JSONArray();
    	}
    	
    	// Read Blockchain
    	JSONArray blockchainJSONArray;
    	String blockchain = context.getBean(Util.class).readFile("blockchain.file");
    	if(blockchain != null) {
    	    blockchainJSONArray = new JSONArray(blockchain);
        } else {
            blockchainJSONArray = new JSONArray();
        }
    	
    	context.getBean(BlockchainService.class).populateOpenTransactions(openTransactionsArray);
    	context.getBean(BlockchainService.class).populateBlockchain(blockchainJSONArray);
	}
}
