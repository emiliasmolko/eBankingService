package service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/")
public class EBankingApplication extends Application {
	
	public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        
        // Resources
        classes.add( PaymentService.class );
        classes.add( AwizoService.class );
        //Loaders
        return classes;
    }
	
}
