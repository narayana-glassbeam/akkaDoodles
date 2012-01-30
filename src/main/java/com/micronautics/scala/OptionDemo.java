package com.micronautics.scala;

import scala.Function0;
import scala.Option;
import scala.Some;
import scala.None;

public class OptionDemo {
    final Option<String> object1 = new Some<String>("Hi there");
    
    final Option<String> object2 = Option.apply(null);
    
    final Option<String> osName =
      new Some<String>(System.getProperty("os.name"));
    
    final Function0<Option<String>> elseOption = new Function0<Option<String>>() { 
    	@Override public Option<String> apply() { 
		    return new Some<String>("default"); 
		}
	};
    
    final Option<String> notPresent =
      new Some<String>(System.getProperty("not.present")).
      orElse(elseOption).get();
    
    public static void main(String[] args) {
    	OptionDemo od = new OptionDemo();
    	System.out.println("object1=" + od.object1);
    	System.out.println("object1=" + od.object2);
    	System.out.println("object1=" + od.osName);
    	System.out.println("object1=" + od.notPresent);
    }
}
