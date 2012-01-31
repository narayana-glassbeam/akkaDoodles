package com.micronautics.scala;

import scala.Option;
import scala.Some;
import scala.runtime.AbstractFunction0;

public class OptionDemo {
    final Option<String> object1 = new Some<String>("Hi there");

    final Option<String> object2 = Option.apply(null);

    final Option<String> osName =
      new Some<String>(System.getProperty("os.name"));

    final AbstractFunction0<String> elseOption = new AbstractFunction0<String>() {
    	@Override public String apply() {
		    return "default";
		}
	};

    final String notPresent =
      (new Some<String>(System.getProperty("not.present"))).getOrElse(elseOption);

    public static void main(String[] args) {
    	OptionDemo od = new OptionDemo();
    	System.out.println("object1 value:    " + od.object1.get());
    	System.out.println("object2 (None):   " + od.object2);
    	System.out.println("osName value:     " + od.osName.get());
    	System.out.println("notPresent value: " + od.notPresent);
    }
}
