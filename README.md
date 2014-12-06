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