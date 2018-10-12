package dao;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Init;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;


@Stateless
@LocalBean
@Resource(type=DataSource.class, name="jdbc/bankDataSource", lookup="jdbc/bankDataSource")

public class BankAccountDAO {

	private static final String CLASS_NAME = BankAccountDAO.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	@PersistenceContext	
	EntityManager entitymanager;
	
	@PostConstruct
	public void init() {
		entitymanager.persist(new BankAccount("Jan","Kowalski",9000,"jk@gmail.com","123456"));
		entitymanager.persist(new BankAccount("Edward", "Kozlowski", 1000000,"ek@gmail.com","123456"));
		entitymanager.persist(new BankAccount("Emilia", "Sokolowska", 1000,"es@gmail.com","123456"));
		entitymanager.persist(new BankAccount("Maria", "Nowak", 879990,"mn@gmail.com","123456"));


		LOGGER.logp(Level.FINEST, CLASS_NAME, "init():", "Persisted");	
	}

	public BankAccount getBankAccount(String name, String surname) {
		//entitymanager.getTransaction( ).begin( );
		Query q = entitymanager.createQuery("SELECT b from BankAccount b where b.name = '" +name+"' and b.surname = '" +surname+"'");
		//entitymanager.getTransaction().commit();
		return (BankAccount) q.getSingleResult();
	}
	
	public BankAccount getBankAccountLogIn(String email, String password) {
		//entitymanager.getTransaction( ).begin( );
		Query q = entitymanager.createQuery("SELECT b from BankAccount b where b.email = '" +email+"' and b.password = '" +password+"'");
		//entitymanager.getTransaction().commit();
		return (BankAccount) q.getSingleResult();
	}
	
	public BankAccount getBankAccountLogIn(String email) {
		//entitymanager.getTransaction( ).begin( );
		Query q = entitymanager.createQuery("SELECT b from BankAccount b where b.email = '" +email+"'");
		//entitymanager.getTransaction().commit();
		return (BankAccount) q.getSingleResult();
	}
}
