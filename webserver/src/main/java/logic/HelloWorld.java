package logic;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "helloWorld")
@ApplicationScoped
public class HelloWorld {

   private int counter = 0;
   private String name;

   
   
   public HelloWorld() {

   }
	
   public String getMessage() {
        return "Hello World "+counter+++"!";
   }
   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}