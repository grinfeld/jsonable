[![Build Status](https://travis-ci.org/grinfeld/jsonable.svg?branch=master)](https://travis-ci.org/grinfeld/jsonable)

jsonable
========

Small library to convert Java POJO to and from JSON

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
    @JsonClass
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

Sometimes, you want to avoid some of variables to be serialized to JSON, so simply, don't add annotation to field, you don't want to be serialized:

    package mypackage;
    public class Foo {
       @JsonField String password = "Hello";
       int num = 1;
    }

    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write(new Foo(), sb);
      System.out.println(sb.toString());
      // {"str":"Hello", "class":"mypackage.Foo"}
    } catch (Exception ignore) {}

Sometimes, we store complex data, but in JSON it must be represented by single value, so in this case you can use **@CustomField** annotation (despite of its name, this annotation is for methods):

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

If you don't want to expose class name when converting objects to JSON, use **ConfInfo**:

    package mypackage;
    @JsonClass
    public class Foo {
       @JsonField String str = "Hello";
       @JsonField int num = 1;
    }
    StringBuilder sb = new StringBuilder();
    try {
      ConfInfo.setExcludeClass(true);
      JsonWriter.write(new Foo(), sb, c);
      System.out.println(sb.toString());
      // {"str":"Hello","num":1}
    } catch (Exception ignore) {}

Another use for ConfInfo: if you need to include **NULL** values in Map or annotated by @JsonClass Object, use **ConfInfo.setIncludeNull(true)**
*Note:* default behavior will not include NULLs

default behavior is writing output without **NULL** fields:

    package mypackage;
    @JsonClass
    public class Foo {
       @JsonField String str = null;
       @JsonField int num = 1;
    }
    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write(new Foo(), sb);
      System.out.println(sb.toString());
      // {"num":1, "class": "mypackage.Foo"}
    } catch (Exception ignore) {}

writing **NULL** for specific field only

    package mypackage;
    @JsonClass
    public class Foo {
       @JsonField @DisplayNull String str = null;
       @JsonField int num = 1;
    }
    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write(new Foo(), sb);
      System.out.println(sb.toString());
      // {"str": null, "num":1, "class": "mypackage.Foo"}
    } catch (Exception ignore) {}

always writing **NULL**

    package mypackage;
    @JsonClass
    public class Foo {
       @JsonField String str = null;
       @JsonField int num = 1;
    }
    StringBuilder sb = new StringBuilder();
    try {
      ConfInfo.setIncludeNull(true);
      JsonWriter.write(new Foo(), sb, c);
      System.out.println(sb.toString());
      // {"str":null,"num":1, "class": "mypackage.Foo"}
    } catch (Exception ignore) {}

*Note:* reading such JSON without 'class', will cause Map as output:

    String json = "{\"num\": 1,\"str\":\"Hello\"}";
    Map map = JsonReader.read(json, Map.class);
    System.out.println(map.get("str"); // prints "Mike"
    System.out.println(map.get("num")); // prints "1"

Sometimes you need to expose different fields in different cases. For such use I added groups() attribute for @JsonField and @CustomField. For example: you have administrator who manages other users, but you don't want to show user's password - only user itself could see his own password.
*Note:* if you don't add any group, means always serialize

    package mypackage;
    @JsonClass
    public class User {
	   @JsonField (groups = {"user", "admin"}
       @JsonField String username = "Mike";
       @JsonField (groups = {"user"}
       String password = "1234"; // never use such password
       @JsonField (groups = {"girlfriend"}
       String nickname = "Honey";
    }
    
    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write(new User(), sb, "user", "girlfriend");
      System.out.println(sb.toString());
      // {"username":"Mike","password":"1234", "nickname": "Honey", "class": "mypackage.User"}
      
      JsonWriter.write(new User(), sb, "admin", "girlfriend");
      System.out.println(sb.toString());
      // {"username":"Mike","nickname": "Honey", "class": "mypackage.User"}
    } catch (Exception ignore) {}


Oh, installation?
------------------------
1. Download Jsonable project
2. Take jsonable.jar into your own project path
3. Don't forget about another 2 libs: common-logging and common-lang3

Maven
-------------------------------------------------------

    <dependency>
        <groupId>com.mikerusoft</groupId>
        <artifactId>jsonable</artifactId>
        <version>1.1.0</version>
    </dependency>

Note: Currently the last published version in repository is 1.0.5.
It works almost same as described in README above, except ConfInfo (in older versions is Configuration class. See published in maven javadoc)

Bugs, changes requests
-------------------

Write me to github@mikerusoft.com
