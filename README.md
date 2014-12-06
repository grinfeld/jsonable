jsonable
========

Small library to convert Java pure objects to and from JSON

    OutputStream out = ....
    try {
      JsonWriter.write("Hello", out);
    } catch (Exception ignore) {}

    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write("Hello", sb);
    } catch (Exception ignore) {}

When you convert java pure object, 'class' parameter will be added into JSON object:

    package mypackage;
    public class Foo {
       String str = "Hello";
       int num = 1;
    }
    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write(new Foo(), sb);
      System.out.println(sb.toString());
      // {"str":"Hello","num":1,"class":"mypackage.Foo"}
    } catch (Exception ignore) {}

So, if you want to read data into appropriate object, do following: 

    String json = "{\"str\":\"Hello\",\"num\":1,\"class\":\"mypackage.Foo\"}";
    Foo foo = JsonReader.read(json, Foo.class);
    System.out.println(foo.str); // prints "Hello"
    System.out.println(foo.num); // prints "1"

Sometimes, you want to avoid some of variables to be serialized to JSON:

    package mypackage;
    public class Foo {
       String password = "My Password";
       int num = 1;
    }

In this case you can use java *transient* modifier or add @IgnoreJson annotation, like this:

    package mypackage;
    public class Foo {
       @IgnoreJson String password = "My Password";
       int num = 1;
    }
    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write(new Foo(), sb);
      System.out.println(sb.toString());
      // {"num":1,"class":"mypackage.Foo"}
    } catch (Exception ignore) {}

Other way to define Object and specific fields to be converted to JSON, it's to use @JsonClass and @JsonField annotations. If you defined class with @JsonClass, you'll need to define every field you want to be converted into JSON

    package mypackage;
    @JsonCLass
    public class Foo {
       @JsonField String name= "Mike";
       int num = 1;
    }
    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write(new Foo(), sb);
      System.out.println(sb.toString());
      // {"name":"Mike","class":"mypackage.Foo"}
    } catch (Exception ignore) {}

Sometimes, we store complex data, but in JSON it must be represented by single value, so in this case you can use @CustomField annotation (despite of its name, this annotation is for methods):

    package mypackage;
	public enum Action {
		FIRST, SECOND, THIRD;
	}
    @JsonCLass
    public class Foo {
       @JsonField String name= "Mike";
       Action action = Action.FIRST;
       
       @CustomField(name = "action")
       public void setAction(int ordinal) {
	       action = Action.values()[ordinal];
       }
       @CustomField(name = "action")
       public int getAction() {
	       return action.ordinal();
       }
    }
    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write(new Foo(), sb);
      System.out.println(sb.toString());
      // {"action": 0,"name":"Mike","class":"mypackage.Foo"}
    } catch (Exception ignore) {}
    
    String json = "{\"action\": 0,\"name\":\"Mike\",\"class\":\"mypackage.Foo\"}";
    Foo foo = JsonReader.read(json, Foo.class);
    System.out.println(foo.name); // prints "Mike"
    System.out.println(foo.action); // prints "FIRST" enum


Oh, installation?
------------------------

1. Download Jsonable project
2. Take jsonable.jar into you own project path
3. Don't forget about another 2 libs: common-logging and common-lang3

Bugs, changes requests
-------------------

Write me to github@mikerusoft.com