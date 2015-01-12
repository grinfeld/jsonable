package com.mikerusoft.jsonable.parser;

import com.mikerusoft.jsonable.annotations.*;
import com.mikerusoft.jsonable.transform.DateTransformer;
import com.mikerusoft.jsonable.utils.Configuration;
import com.mikerusoft.jsonable.utils.ContextManager;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Grinfeld Mikhail
 * @since 12/6/2014.
 */
public class JsonTest {

    @JsonClass
    public static class SimpleObj {
        @JsonField String str;
        @JsonField int num;

        @Override
        public String toString() {
            return "{" +
                "str:\"" + str + '\"' +
                ", num:" + num +
            '}';
        }
    }

    @JsonClass
    public static class SimpleObjCustom extends SimpleObj {
        Object custom;

        @CustomField(name="custom") public Object getCustom() {
            return custom;
        }

        @CustomField(name="custom") public void setCustom(Object custom) {
            this.custom = custom;
        }

        @Override
        public String toString() {
            return "{" +
                "str:\"" + str + '\"' +
                ", num:" + num +
                "custom:\"" + String.valueOf(custom) + '\"' +
            '}';
        }
    }

    @JsonClass
    public static class SimpleObjAnnot {
        @JsonField(name = "str", groups = {"mygroup"}) String str1;
        @JsonField int num;
        String ignore;

        @Override
        public String toString() {
            return "{" +
                    "str:\"" + str1 + '\"' +
                    ", num:" + num +
                    '}';
        }
    }

    @JsonClass
    public static class SimpleObjAnnotExtend  extends SimpleObjAnnot {
        @JsonField String extended;
        @JsonField @DateField(type = DateTransformer.TIMESTAMP_TYPE) long time;
    }

    StringBuilder sb = new StringBuilder();
    StringBuilder sb1 = new StringBuilder();
    SimpleObj simpleObj = new SimpleObj();
    SimpleObjAnnot simpleObjAnot = new SimpleObjAnnot();
    SimpleObjAnnotExtend simpleObjAnotExtend = new SimpleObjAnnotExtend();
    SimpleObjCustom simpleObjCustom = new SimpleObjCustom();
    Configuration c = new Configuration();

    @Before
    public void cleanBuilder() {
        sb = new StringBuilder();
        simpleObj = new SimpleObj();
        simpleObjAnot = new SimpleObjAnnot();
        simpleObjAnotExtend = new SimpleObjAnnotExtend();
        simpleObjCustom = new SimpleObjCustom();
        sb1 = new StringBuilder();
        ContextManager.unset();
    }

    @Test public void stringTest() {
        try {
            JsonWriter.write("Hello", sb);
        } catch (Exception ignore) {}
        assertEquals("Failed StringTest" + sb.toString(), "\"Hello\"", sb.toString());
    }

    @Test public void integerTest() {
        try {
            JsonWriter.write(1, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed integer test " + sb.toString(), "1", sb.toString());
    }

    @Test public void longTest() {
        try {
            JsonWriter.write(1l, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed long test " + sb.toString(), "1", sb.toString());
    }

    @Test public void shortTest() {
        try {
            JsonWriter.write((short)1, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed short test " + sb.toString(), "1", sb.toString());
    }

    @Test public void booleanFalseTest() {
        try {
            JsonWriter.write(false, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed boolean false test " + sb.toString(), "false", sb.toString());
    }

    @Test public void booleanTrueTest() {
        try {
            JsonWriter.write(true, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed boolean true test " + sb.toString(), "true", sb.toString());
    }

    @Test public void simpleArrayStringTest() {
        try {
            JsonWriter.write(new String[] {"hello", "bye"}, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple array string test " + sb.toString(), "[\"hello\",\"bye\"]", sb.toString());
    }

    @Test public void simpleArrayNumberTest() {
        try {
            JsonWriter.write(new int[] {1, 2}, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple array number test " + sb.toString(), "[1,2]", sb.toString());
    }


    @Test public void simpleListStringTest() {
        try {
            JsonWriter.write(Arrays.asList("hello", "bye"), sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple array string test " + sb.toString(), "[\"hello\",\"bye\"]", sb.toString());
    }

    @Test public void simpleListNumberTest() {
        try {
            JsonWriter.write(Arrays.asList(1, 2), sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple array number test " + sb.toString(), "[1,2]", sb.toString());
    }

    @Test public void simpleObjectTest() {
        try {
            simpleObj.str = "Hello";
            simpleObj.num = 1;
            JsonWriter.write(simpleObj, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple object test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.parser.JsonTest$SimpleObj\"}", sb.toString());
    }

    @Test public void simpleObjectExcludeClassTest() {
        try {
            simpleObj.str = "Hello";
            simpleObj.num = 1;
            c.setProperty(Configuration.EXCLUDE_CLASS_PROPERTY, "true");
            JsonWriter.write(simpleObj, sb, c);
        } catch (Exception ignore) {}
        assertEquals("Failed simple object exclude class test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1}", sb.toString());  ;
    }

    @Test public void simpleObjectAnotTest() {
        try {
            simpleObjAnot.str1 = "Hello";
            simpleObjAnot.num = 1;
            simpleObjAnot.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnot, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple object with annotation test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.parser.JsonTest$SimpleObjAnnot\"}", sb.toString());
    }

    @Test public void simpleObjectAnotWithGroupTest() {
        try {
            simpleObjAnot.str1 = "Hello";
            simpleObjAnot.num = 1;
            simpleObjAnot.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnot, sb, "mygroup");

            Object o = JsonReader.read(sb.toString(), "mygroup");
            JsonWriter.write(o, sb1);
        } catch (Exception ignore) {}
        assertEquals("Failed object with mygroup" + sb.toString(), "{\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.parser.JsonTest$SimpleObjAnnot\"}", sb.toString());
    }

    @Test public void simpleObjectAnotExcludeClassTest() {
        try {
            simpleObjAnot.str1 = "Hello";
            simpleObjAnot.num = 1;
            c.setProperty(Configuration.EXCLUDE_CLASS_PROPERTY, "true");
            JsonWriter.write(simpleObjAnot, sb, c);
        } catch (Exception ignore) {}
        assertEquals("Failed simple object with annotation exclude class test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1}", sb.toString());
    }


    @Test public void simpleObjectExtendedTest() {
        long t = new Date().getTime();
        try {
            simpleObjAnotExtend.str1 = "Hel\"lo";
            simpleObjAnotExtend.num = 1;
            simpleObjAnotExtend.extended = "Extended";
            simpleObjAnotExtend.ignore = "Ignore me";
            simpleObjAnotExtend.time = t;
            JsonWriter.write(simpleObjAnotExtend, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple object extended test " + sb.toString(), "{\"extended\":\"Extended\",\"time\":" + t + ",\"str\":\"Hel\\\"lo\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.parser.JsonTest$SimpleObjAnnotExtend\"}", sb.toString());
    }


    @Test public void simpleObjectExtendedWithNullTest() {
        try {
            simpleObjAnotExtend.str1 = "Hello";
            simpleObjAnotExtend.num = 1;
            simpleObjAnotExtend.extended = null;
            simpleObjAnotExtend.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnotExtend, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple object extended test " + sb.toString(), "{\"extended\":null,\"time\":0,\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.parser.JsonTest$SimpleObjAnnotExtend\"}", sb.toString());
    }

    @Test public void simpleObjectExtendedWithEmptStringTest() {
        try {
            simpleObjAnotExtend.str1 = "Hello";
            simpleObjAnotExtend.num = 1;
            simpleObjAnotExtend.extended = "";
            simpleObjAnotExtend.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnotExtend, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple object extended test " + sb.toString(), "{\"extended\":\"\",\"time\":0,\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.parser.JsonTest$SimpleObjAnnotExtend\"}", sb.toString());
    }

    @Test public void simpleObjectCustomTest() {
        try {
            simpleObjCustom.str = "Hello";
            simpleObjCustom.num = 1;
            simpleObjCustom.custom = "Custom me";
            JsonWriter.write(simpleObjCustom, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple object with custom test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1,\"custom\":\"Custom me\",\"class\":\"com.mikerusoft.jsonable.parser.JsonTest$SimpleObjCustom\"}", sb.toString());
    }


    @Test public void mapTest() {
        try {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("str", "Hello");
            m.put("num", 1);
            JsonWriter.write(m, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed map test " + sb.toString(), "{\"num\":1,\"str\":\"Hello\"}", sb.toString());
    }


    @Test public void stringWriteReadTest() {
        try {
            JsonWriter.write("Hello", sb);
            Object o = JsonReader.read(sb.toString());
            JsonWriter.write(o, sb1);
        } catch (Exception ignore) {}
        assertEquals("stringWriteReadTest", sb1.toString(), sb.toString());
    }

    @Test public void numberWriteReadTest() {
        try {
            JsonWriter.write(1, sb);
            Object o = JsonReader.read(sb.toString());
            JsonWriter.write(o, sb1);
        } catch (Exception ignore) {}
        assertEquals("numberWriteReadTest", sb1.toString(), sb.toString());
    }

    @Test public void readWriteSimpleAnotTest() {
        try {
            simpleObjAnotExtend.str1 = "Hel\"lo";
            simpleObjAnotExtend.num = 1;
            simpleObjAnotExtend.extended = "Extended";
            JsonWriter.write(simpleObjAnotExtend, sb);
            Object o = JsonReader.read(sb.toString());
            JsonWriter.write(o, sb1);
        } catch (Exception ignore) {}
        assertEquals("readWriteSimpleAnotTest", sb1.toString(), sb.toString());
    }

    @Test public void numberArrayTest() {
        try {
            JsonWriter.write(new Object[] {1,2,3}, sb);
        } catch (Exception ignore) {}
        assertEquals("numberArrayTest", "[1,2,3]", sb.toString());
    }

    @Test public void numberListTest() {
        try {
            JsonWriter.write(Arrays.asList(new Integer[] {1,2,3}), sb);
        } catch (Exception ignore) {}
        assertEquals("numberListTest", "[1,2,3]", sb.toString());
    }

    @Test public void numberSetTest() {
        try {
            JsonWriter.write(new HashSet<Integer>(Arrays.asList(new Integer[] {1,2,3})), sb);
        } catch (Exception ignore) {}
        assertEquals("numberListTest", "[1,2,3]", sb.toString());
    }
}
