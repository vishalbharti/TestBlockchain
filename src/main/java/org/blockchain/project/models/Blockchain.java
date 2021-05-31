package org.blockchain.project.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Blockchain {

    private List<Block> blockchain = new ArrayList<>();
    private List<Transaction> openTransactions = new ArrayList<>();
    private String difficulty;
    
    public List<Block> getBlockchain() {
        return blockchain;
    }
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }
    
    public List<Transaction> getOpenTransactions() {
        return openTransactions;
    }
    public void setOpenTransactions(List<Transaction> openTransactions) {
        this.openTransactions.addAll(openTransactions);
    }
    
	public String getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
}
