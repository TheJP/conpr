/*
 * Copyright (c) 2000-2015 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements bank.BankDriver {
	private Bank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new Bank();
		System.out.println("connected...");
	}

	@Override
	public void disconnect() {
		bank = null;
		System.out.println("disconnected...");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	static class Bank implements bank.Bank {

		/**
		 * Lock for structural changes on the account map itself. Content of the accounts are locked independently.
		 */
		private Lock mapStructureLock = new ReentrantLock();

		//MessageDigest and Random are used to generate the Account Number
		private static final int BUFFER_SIZE = 512;
		private MessageDigest digest;
		private Random r = new Random();
		private byte[] buffer = new byte[BUFFER_SIZE];
		
		//@GuardedBy("mapStructureLock")
		private final Map<String, Account> accounts = new HashMap<String, Account>();

		public Bank() {
			try {
				digest = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Set<String> getAccountNumbers() {
			mapStructureLock.lock();
			try {
				Set<String> result = new HashSet<String>();
				for(Account a : accounts.values()){
					if(a.isActive()){ result.add(a.getNumber()); }
				}
				return result;
			} finally {
				mapStructureLock.unlock();
			}
		}

		@Override
		public String createAccount(String owner) {
			mapStructureLock.lock();
			try {
				r.nextBytes(buffer);
				String number = new String(digest.digest(buffer));
				accounts.put(number, new Account(owner, number));
				return number;
			} finally {
				mapStructureLock.unlock();
			}
		}

		@Override
		public boolean closeAccount(String number) {
			mapStructureLock.lock();
			try {
				Account a = accounts.get(number);
				a.accountLock.lock();
				try {
					if(!a.isActive()){
						return false;
					}
					if(a.getBalance() == 0){
						a.active = false;
					}
					return !a.isActive();
				} finally {
					a.accountLock.unlock();
				}
			} finally {
				mapStructureLock.unlock();
			}
		}

		@Override
		public bank.Account getAccount(String number) {
			mapStructureLock.lock();
			try {
				return accounts.get(number);
			} finally {
				mapStructureLock.unlock();
			}
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount) throws IOException, InactiveException, OverdrawException {
			int i = from.getNumber().compareTo(to.getNumber());
			bank.Account a, b;
			if(i > 0){ a = from; b = to; }
			else { a = to; b = from; }
			if(!(from instanceof Account) || !(to instanceof Account)){ throw new IOException("Instance is not supported in this Bank"); }
			((Account)a).accountLock.lock();
			((Account)b).accountLock.lock();
			try {
				if(!to.isActive()){ throw new InactiveException(); }
				from.withdraw(amount);
				to.deposit(amount);
			} finally {
				((Account)a).accountLock.unlock();
				((Account)b).accountLock.unlock();
			}
		}

	}

	static class Account implements bank.Account {
		private String number;
		private String owner;
		private double balance = 0;
		private boolean active = true;

		private Lock accountLock = new ReentrantLock();

		Account(String owner, String number) {
			this.owner = owner;
			this.number = number;
		}

		@Override
		public double getBalance() {
			accountLock.lock();
			try {
				return balance;
			} finally {
				accountLock.unlock();
			}
		}

		@Override
		public String getOwner() {
			return owner; //immutable -> not locked
		}

		@Override
		public String getNumber() {
			return number; //immutable -> not locked
		}

		@Override
		public boolean isActive() {
			accountLock.lock();
			try {
				return active;
			} finally {
				accountLock.unlock();
			}
		}

		@Override
		public void deposit(double amount) throws InactiveException {
			accountLock.lock();
			try {
				if(!active){ throw new InactiveException(); }
				if(amount < 0){ throw new IllegalArgumentException(); }
				balance += amount;
			} finally {
				accountLock.unlock();
			}
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException {
			accountLock.lock();
			try {
				if(!active){ throw new InactiveException(); }
				if(amount < 0){ throw new IllegalArgumentException(); }
				if(amount > balance){ throw new OverdrawException(); }
				balance -= amount;
			} finally {
				accountLock.unlock();
			}
		}

	}

}