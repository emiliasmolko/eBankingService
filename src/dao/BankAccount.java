package dao;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table( name = "ACCOUNT" )
public class BankAccount implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO) 	
	private int id;
	
	private String name;
	private String surname;
	private int balance;
	private String email;
	private String password;
	
	public BankAccount(String name, String surname,int balance, String email, String password) {
		this.name = name;
		this.surname = surname;
		this.balance = balance;
		this.email = email;
		this.password = password;
	}
	
	public BankAccount() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
}
