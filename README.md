jsonable
========

Small Java based parser Java pure objects to and from JSON

1. Simple use: parse only Maps and Arrays of primitive and strings

    OutputStream out = ....
    try {
      JsonWriter.write("Hello", out);
    } catch (Exception ignore) {}

    StringBuilder sb = new StringBuilder();
    try {
      JsonWriter.write("Hello", sb);
    } catch (Exception ignore) {}
    
2. Parse Objects    
