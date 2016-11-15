package com.mikerusoft.jsonable;

import com.mikerusoft.jsonable.adapters.ReadInstanceFactory;
import com.mikerusoft.jsonable.adapters.SimpleBeanAdapter;
import com.mikerusoft.jsonable.annotations.*;
import com.mikerusoft.jsonable.parser.JsonReader;
import com.mikerusoft.jsonable.parser.JsonWriter;
import com.mikerusoft.jsonable.transform.DateTransformer;
import com.mikerusoft.jsonable.utils.ConfInfo;
import com.mikerusoft.jsonable.utils.Configuration;
import com.mikerusoft.jsonable.utils.PropertyPair;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
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

    public static class SimpleObjNoAnot {
        String str;
        int num;
        boolean bool;

        public String getStr() { return str; }
        public void setStr(String str) { this.str = str; }
        public int getNum() { return num; }
        public void setNum(int num) { this.num = num; }
        public boolean isBool() { return bool; }
        public void setBool(boolean bool) { this.bool = bool; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleObjNoAnot that = (SimpleObjNoAnot) o;

            if (num != that.num) return false;
            if (bool != that.bool) return false;
            return str != null ? str.equals(that.str) : that.str == null;

        }

        @Override
        public String toString() {
            return "{" +
                    "str:\"" + str + '\"' +
                    ", num:" + num +
                    ", bool:" + bool +
                    '}';
        }
    }

    @JsonClass
    public static class SimpleObjDouble extends SimpleObj {
        @JsonField Float floating;

        @Override
        public String toString() {
            return "{" +
                "str:\"" + str + '\"' +
                ", num:" + num +
                ", floating:" + floating +
            '}';
        }
    }

    @JsonClass
    public static class SimpleObjCustom extends SimpleObj {
        Long custom;

        @CustomField(name="custom") public Long getCustom() {
            return custom;
        }

        @CustomField(name="custom") public void setCustom(Long custom) {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleObjAnnot that = (SimpleObjAnnot) o;

            if (num != that.num) return false;
            return str1 != null ? str1.equals(that.str1) : that.str1 == null;

        }

        @Override
        public int hashCode() {
            return str1.hashCode();
        }
    }

    @JsonClass
    public static class SimpleObjAnnotExtend  extends SimpleObjAnnot {
        @JsonField String extended;
        @JsonField @DateField(type = DateTransformer.TIMESTAMP_TYPE) long time;
    }

    @JsonClass
    public static class SimpleObjAnnotDateString {
        @JsonField String value;
        @JsonField @DateField(type = DateTransformer.STRING_TYPE, format = "yyyyMMDD") Date date;
    }

    @JsonClass
    public static class SimpleObjAnnotExtendWithAnnotNull  extends SimpleObjAnnot {
        @JsonField @DisplayNull String extendedNull;
        @JsonField @DateField(type = DateTransformer.TIMESTAMP_TYPE) long time;
    }

    @JsonClass
    public static class SimpleObjEnum {

        public enum MyEnum {
            M1, M2;
        }

        @JsonField String str;
        @JsonField MyEnum en;

        @Override
        public String toString() {
            return "{" +
                    "str:\"" + String.valueOf(str) + '\"' +
                    ", en:\"" + (en != null ? en.name() : "") + '\"' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleObjEnum that = (SimpleObjEnum) o;

            if (str != null ? !str.equals(that.str) : that.str != null) return false;
            return en == that.en;

        }
    }

    @JsonClass
    public static class ListObj {
        @JsonField String str;
        @JsonField List<Integer> list;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{" +
                    "str:\"" + str + '\"' +
                    ", list:");
            if (list == null || list.size() == 0)
                sb.append("[]");
            else {
                int count = 0;
                for(Integer i : list) {
                    if (count > 0)
                        sb.append(",");
                    sb.append(i);
                    count++;
                }

            }
            sb.append('}');

            return sb.toString();
        }
    }

    public enum TestEnum {
        Hello
    }

    public static class Pair<T, K> {
        T left;
        K right;

        public Pair() {}

        public Pair(T left, K right) {
            this.left = left;
            this.right = right;
        }

        public T getLeft() { return left; }
        public void setLeft(T left) { this.left = left; }
        public K getRight() { return right; }
        public void setRight(K right) { this.right = right; }

        @Override
        public String toString() {
            return "Pair{" +
                    "left=" + String.valueOf(left) +
                    ", right=" + String.valueOf(right) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (!left.equals(pair.left)) return false;
            return right.equals(pair.right);

        }

        @Override
        public int hashCode() {
            int result = left.hashCode();
            result = 31 * result + right.hashCode();
            return result;
        }
    }

    StringBuilder sb = new StringBuilder();
    StringBuilder sb1 = new StringBuilder();
    SimpleObj simpleObj = new SimpleObj();
    SimpleObjDouble simpleObjDouble = new SimpleObjDouble();
    SimpleObjAnnot simpleObjAnot = new SimpleObjAnnot();
    SimpleObjAnnotExtend simpleObjAnotExtend = new SimpleObjAnnotExtend();
    SimpleObjAnnotExtendWithAnnotNull simpleObjAnotExtendWithNull = new SimpleObjAnnotExtendWithAnnotNull();
    SimpleObjCustom simpleObjCustom = new SimpleObjCustom();
    SimpleObjAnnotDateString simpleObjDateString = new SimpleObjAnnotDateString();
    Configuration c = new Configuration();
    SimpleObjNoAnot simpleNoAnot = new SimpleObjNoAnot();
    ListObj listObj = new ListObj();

    @Before
    public void cleanBuilder() {
        sb = new StringBuilder();
        simpleObj = new SimpleObj();
        simpleObjDouble = new SimpleObjDouble();
        simpleObjAnot = new SimpleObjAnnot();
        simpleObjAnotExtend = new SimpleObjAnnotExtend();
        simpleObjAnotExtendWithNull = new SimpleObjAnnotExtendWithAnnotNull();
        simpleObjCustom = new SimpleObjCustom();
        simpleObjDateString = new SimpleObjAnnotDateString();
        simpleNoAnot = new SimpleObjNoAnot();
        sb1 = new StringBuilder();
        listObj = new ListObj();
        c = new Configuration();
        ConfInfo.unset();
    }

    @Test public void stringToStreamTest() {
        String result = "";
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            JsonWriter.write("Hello", b, "UTF-8");
            result = new String(b.toByteArray());
        } catch (Exception ignore) {}
        assertEquals("Failed StringTest" + result, "\"Hello\"", result);
    }

    @Test public void integerToStreamTest() {
        String result = "";
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            JsonWriter.write(1, b, "UTF-8");
            result = new String(b.toByteArray());
        } catch (Exception ignore) {}
        assertEquals("Failed integer test " + result, "1", result);
    }


    @Test public void mapUnicodeTest() {
        String text = "שלום";
        Map<String, String> m = new HashMap<>();
        m.put(text, "1");
        try {
            JsonWriter.write(m, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed stringUnicodeTest" + sb.toString(), "{\"שלום\":\"1\"}", sb.toString());
        String result = null;
        try {
            m = (Map<String,String>)JsonReader.read(sb.toString(), Map.class);
        } catch (Exception ignore) {}
        assertEquals("Failed stringUnicodeTest" + text, text, m.keySet().iterator().next());
        assertEquals("Failed stringUnicodeTest" + text, "1", m.get(text));
    }

    @Test public void stringUnicodeTest() {
        String text = "שלום";
        try {
            JsonWriter.write(text, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed stringUnicodeTest" + sb.toString(), "\"" + text + "\"", sb.toString());
        String result = null;
        try {
            result = JsonReader.read(sb.toString(), String.class);
        } catch (Exception ignore) {}
        assertEquals("Failed stringUnicodeTest" + text, text, result);
    }

    @Test public void stringTest() {
        try {
            JsonWriter.write("Hello", sb);
        } catch (Exception ignore) {}
        assertEquals("Failed StringTest" + sb.toString(), "\"Hello\"", sb.toString());
    }

    @Test public void stringAposTest() throws IOException {
        Map<String, String> m = (Map<String, String>)JsonReader.read("{\"key\": \"Hel'lo\"}", Map.class);
        assertNotNull(m);
        assertNotNull(m.get("key"));
        assertEquals("Failed stringAposTest" + "{\"key\": \"Hel'lo\"}", "Hel'lo", m.get("key"));
    }

    @Test public void stringAposWriteTest() throws IOException {
        try {
            JsonWriter.write("Hel'lo", sb);
        } catch (Exception ignore) {}
        assertEquals("Failed StringTest" + sb.toString(), "\"Hel'lo\"", sb.toString());;
    }

    @Test public void integerTest() {
        try {
            JsonWriter.write(1, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed integer test " + sb.toString(), "1", sb.toString());
    }

    @Test public void unicodeTest() {
        try {
            JsonWriter.write("Привет", sb);
        } catch (Exception ignore) {}
        assertEquals("failed unicode test " + sb.toString(), "\"Привет\"", sb.toString());
    }

    @Test public void integerWithClassTest() {
        try {
            ConfInfo.setIncludePrimitiveClass(true);
            JsonWriter.write(1, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed integer test " + sb.toString(), "{\"value\": 1,\"class\":\"java.lang.Integer\"}", sb.toString());
        Integer i = null;
        try {
            i = JsonReader.read(sb.toString(), Integer.class);
        } catch (Exception ignore) {}
        assertEquals("Should be 1, but " + String.valueOf(i), new Integer(1), i);
    }

    @Test public void longTest() {
        try {
            JsonWriter.write(1L, sb);
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

    @Test public void booleanFalseWithClassTest() throws IOException {
        try {
            ConfInfo.setIncludePrimitiveClass(true);
            JsonWriter.write(false, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed boolean false test " + sb.toString(), "{\"value\": false,\"class\":\"java.lang.Boolean\"}", sb.toString());

        Boolean res = JsonReader.read(sb.toString(), Boolean.class);
        assertEquals(false, res);
        sb = new StringBuilder();
        try {
            ConfInfo.setIncludePrimitiveClass(true);
            JsonWriter.write(new Object[] { false }, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed boolean false test " + sb.toString(), "[{\"value\": false,\"class\":\"java.lang.Boolean\"}]", sb.toString());
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

    @Test public void objectArrayTest() {
        try {
            JsonWriter.write(new Object[] {"hello", 1L, true}, sb);
        } catch (Exception ignore) {}
        assertEquals("Failed simple array string test " + sb.toString(), "[\"hello\",1,true]", sb.toString());

        List ar = new ArrayList<>(0);
        try {
            ar = JsonReader.read(sb.toString(), List.class);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        assertNotNull(ar);
        assertEquals("Size should be 3 for " + sb.toString(), 3, ar.size());
        assertEquals("hello", ar.get(0));
        assertEquals(1L, ar.get(1));
        assertEquals(true, ar.get(2));
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

    @Test public void simpleObjectEndLineTest() {
        try {
            simpleObj.str = "Hello\n";
            simpleObj.num = 1;
            JsonWriter.write(simpleObj, sb);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object test " + sb.toString(), "{\"str\":\"Hello\\n\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObj\"}", sb.toString());
    }

    @Test public void simpleObjectTest() {
        try {
            simpleObj.str = "Hello";
            simpleObj.num = 1;
            JsonWriter.write(simpleObj, sb);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObj\"}", sb.toString());
    }

    @Test public void simpleObjectExcludeClassOldTest() {
        try {
            simpleObj.str = "Hello";
            simpleObj.num = 1;
            c.setProperty(Configuration.EXCLUDE_CLASS_PROPERTY, "true");
            JsonWriter.write(simpleObj, sb, c);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object exclude class test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1}", sb.toString());  ;
    }

    @Test public void simpleObjectExcludeClassTest() {
        try {
            simpleObj.str = "Hello";
            simpleObj.num = 1;
            ConfInfo.setExcludeClass(true);
            JsonWriter.write(simpleObj, sb, c);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object exclude class test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1}", sb.toString());  ;
    }

    @Test public void simpleObjectAnotTest() {
        try {
            simpleObjAnot.str1 = "Hello";
            simpleObjAnot.num = 1;
            simpleObjAnot.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnot, sb);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object with annotation test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnot\"}", sb.toString());
    }

    @Test public void simpleObjectAnotWithGroupTest() {
        try {
            simpleObjAnot.str1 = "Hello";
            simpleObjAnot.num = 1;
            simpleObjAnot.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnot, sb, "mygroup");

            Object o = JsonReader.read(sb.toString(), "mygroup");
            JsonWriter.write(o, sb1);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        assertSplitSimpleJson("Failed object with mygroup" + sb.toString(), "{\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnot\"}", sb.toString());
    }

    @Test public void simpleObjectAnotExcludeClassOldTest() {
        try {
            simpleObjAnot.str1 = "Hello";
            simpleObjAnot.num = 1;
            c.setProperty(Configuration.EXCLUDE_CLASS_PROPERTY, "true");
            JsonWriter.write(simpleObjAnot, sb, c);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object with annotation exclude class test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1}", sb.toString());
    }

    @Test public void simpleObjectAnotExcludeClassTest() {
        try {
            simpleObjAnot.str1 = "Hello";
            simpleObjAnot.num = 1;
            ConfInfo.setExcludeClass(true);
            JsonWriter.write(simpleObjAnot, sb, c);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object with annotation exclude class test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1}", sb.toString());
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
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        assertSplitSimpleJson("Failed simple object extended test " + sb.toString(), "{\"extended\":\"Extended\",\"time\":" + t + ",\"str\":\"Hel\\\"lo\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnotExtend\"}", sb.toString());
    }


    @Test public void simpleObjectExtendedWithIgnoreNullTest() {
        try {
            simpleObjAnotExtend.str1 = "Hello";
            simpleObjAnotExtend.num = 1;
            simpleObjAnotExtend.extended = null;
            simpleObjAnotExtend.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnotExtend, sb);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object extended test " + sb.toString(), "{\"time\":0,\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnotExtend\"}", sb.toString());
    }

    @Test public void simpleObjectExtendedWithAnnotNullTest() {
        try {
            simpleObjAnotExtendWithNull.str1 = "Hello";
            simpleObjAnotExtendWithNull.num = 1;
            simpleObjAnotExtendWithNull.extendedNull = null;
            simpleObjAnotExtendWithNull.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnotExtendWithNull, sb, c);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object extended test " + sb.toString(), "{\"extendedNull\":null,\"time\":0,\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnotExtendWithAnnotNull\"}", sb.toString());
    }

    @Test public void simpleObjectExtendedWithNullOldTest() {
        try {
            c.setProperty(Configuration.INCLUDE_NULL_PROPERTY, "true");
            simpleObjAnotExtend.str1 = "Hello";
            simpleObjAnotExtend.num = 1;
            simpleObjAnotExtend.extended = null;
            simpleObjAnotExtend.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnotExtend, sb, c);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object extended test " + sb.toString(), "{\"extended\":null,\"time\":0,\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnotExtend\"}", sb.toString());
    }

    @Test public void simpleObjectExtendedWithNullTest() {
        try {
            ConfInfo.setIncludeNull(true);
            simpleObjAnotExtend.str1 = "Hello";
            simpleObjAnotExtend.num = 1;
            simpleObjAnotExtend.extended = null;
            simpleObjAnotExtend.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnotExtend, sb, c);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object extended test " + sb.toString(), "{\"extended\":null,\"time\":0,\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnotExtend\"}", sb.toString());
    }

    @Test public void simpleObjectExtendedWithEmptyStringTest() {
        try {
            simpleObjAnotExtend.str1 = "Hello";
            simpleObjAnotExtend.num = 1;
            simpleObjAnotExtend.extended = "";
            simpleObjAnotExtend.ignore = "Ignore me";
            JsonWriter.write(simpleObjAnotExtend, sb);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object extended test " + sb.toString(), "{\"extended\":\"\",\"time\":0,\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnotExtend\"}", sb.toString());
    }

    @Test public void simpleObjectCustomTest() {
        try {
            simpleObjCustom.str = "Hello";
            simpleObjCustom.num = 1;
            simpleObjCustom.custom = 456L;
            JsonWriter.write(simpleObjCustom, sb);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed simple object with custom test " + sb.toString(), "{\"str\":\"Hello\",\"num\":1,\"custom\":456,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjCustom\"}", sb.toString());
    }

    @Test public void mapTest() {
        try {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("num", 1);
            m.put("str", "Hello");
            JsonWriter.write(m, sb);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed map test " + sb.toString(), "{\"num\":1,\"str\":\"Hello\"}", sb.toString());
    }

    @Test public void stringWithSpacesTest() {
        Map<String, Object> m = new HashMap<>();
        try {
            m.put("str", " Hello ");
            JsonWriter.write(m, sb);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("Failed map test " + sb.toString(), "{\"str\":\" Hello \"}", sb.toString());

        Map<String, Object> m1 = new HashMap<>();
        try {
            m1 = JsonReader.read(sb.toString(), Map.class);
        } catch (Exception ignore){}
        Assert.assertEquals(m.get("str"), m1.get("str"));
    }

    @Test public void stringWriteReadInputStreamTest() {
        try {
            JsonWriter.write("Hello", sb);
            ByteArrayInputStream bys = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
            Object o = JsonReader.read(bys);
            JsonWriter.write(o, sb1);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("stringWriteReadTest", sb1.toString(), sb.toString());
    }

    @Test public void stringWriteReadTest() {
        try {
            JsonWriter.write("Hello", sb);
            Object o = JsonReader.read(sb.toString());
            JsonWriter.write(o, sb1);
        } catch (Exception ignore) {}
        assertSplitSimpleJson("stringWriteReadTest", sb1.toString(), sb.toString());
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
        assertSplitSimpleJson(sb1.toString(), sb.toString());
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
            JsonWriter.write(new HashSet<>(Arrays.asList(new Integer[] {1,2,3})), sb);
        } catch (Exception ignore) {}
        assertEquals("numberListTest", "[1,2,3]", sb.toString());
    }

    @Test public void complStringTest() {
        String str = "Hel\"lo";
        Object o = null;
        try {
            JsonWriter.write(str, sb);
            o = JsonReader.read(sb.toString());
            JsonWriter.write(o, sb1);
        } catch (Exception ignore) {}
        assertNotNull(o);
        assertEquals(str, o);
        assertEquals(sb.toString(), sb1.toString());
    }

    @Test public void doubleTest() {
        try {
            simpleObjDouble.str = "Hel\"lo";
            simpleObjDouble.num = 1;
            simpleObjDouble.floating = (float)1.0;
            JsonWriter.write(simpleObjDouble, sb);
            Object o = JsonReader.read(sb.toString());
            JsonWriter.write(o, sb1);
        } catch (Exception e) {}
        assertSplitSimpleJson(sb1.toString(), sb.toString());
    }

    @Test
    public void enumTest() throws Exception {
        SimpleObjEnum soe = new SimpleObjEnum();
        soe.str = "Hello";
        soe.en = SimpleObjEnum.MyEnum.M1;
        JsonWriter.write(soe, sb);
        SimpleObjEnum soe1 = JsonReader.read(sb.toString(), SimpleObjEnum.class);
        Assert.assertEquals(soe, soe1);
    }

    @Test
    public void map1Test() throws Exception {
        String s = "{\"key1\":  \"hello\", \"key2\"  \n :    {\"key3\"    : \t   5  , \"key4\": [\t\t\"dt\", \"st\", [34, 56]   ]   }     }";
        Map<String, Object> mobj = (Map<String, Object>)JsonReader.read(s);
        Assert.assertNotNull(mobj);
        Assert.assertNotNull(mobj.get("key1"));
        Assert.assertEquals(mobj.get("key1"), "hello");
        Assert.assertNotNull(mobj.get("key2"));

        Map innerMap = ((Map)mobj.get("key2"));
        Assert.assertNotNull(innerMap.get("key3"));
        Assert.assertEquals( innerMap.get("key3"), 5L);

        Assert.assertNotNull( innerMap.get("key4"));

        Assert.assertTrue(innerMap.get("key4") instanceof List);
        List lis = (List)innerMap.get("key4");
        Assert.assertEquals( 3, lis.size());
    }

    @Test
    public void mapArrayTest() throws Exception {
        Map<Object, Object> m = new HashMap<>();
        Map<Object, Object> m2 = new HashMap<>();
        m2.put("value3", "333333");
        m.put("value1",  "111111");
        m.put ("value2", "222222");
        m.put("Data", m2);

        JsonWriter.write(m, sb);
        m = JsonReader.read(sb.toString(), Map.class);
        Assert.assertEquals("", m.get("value1"), "111111");
        Assert.assertEquals("", m.get("value2"), "222222");
        Map ms = (Map)m.get("Data");
        Assert.assertNotNull("", ms);
        Assert.assertEquals("", ms.get("value3"), "333333");
    }

    @Test
    public void dateWithStringFormatTest() throws Exception {
        SimpleObjAnnotDateString m = JsonReader.read("{\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnotDateString\", \n\"date\" : \"20161008\",\n\"value\" : \"Hello\"\n}", SimpleObjAnnotDateString.class);
        Assert.assertNotNull("Shouldn't be null", m);
        Assert.assertEquals(m.value, "Hello");
        Assert.assertEquals(m.date, new SimpleDateFormat("yyyyMMDD").parse("20161008"));
        sb = new StringBuilder();
        JsonWriter.write(m, sb);
        assertSplitSimpleJson("{\"value\":\"Hello\",\"date\":\"20160108\",\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnotDateString\"}", sb.toString());
    }

    @Test
    public void mapWithEndLineTest() throws IOException {
        Map<Object, Object> m = JsonReader.read("{\n\"success\" : true,\n\"msg\" : \"Hello\"\n}", Map.class);
        Assert.assertNotNull("Shouldn't be null", m);
        Assert.assertEquals(m.get("success"), new Boolean(true));
        Assert.assertEquals(m.get("msg"), "Hello");
    }

    @Test
    public void enumAsClassOldTest() throws Exception {
        c.setProperty(Configuration.ENUM_AS_CLASS_PROPERTY, "true");
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(TestEnum.Hello, sb);
        TestEnum en = JsonReader.read(sb.toString(), TestEnum.class);
        Assert.assertNotNull("Shouldn't be null", en);
        Assert.assertEquals(TestEnum.Hello, en);
    }

    @Test
    public void enumNoClassOldTest() throws Exception {
        c.setProperty(Configuration.ENUM_AS_CLASS_PROPERTY, "false");
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(TestEnum.Hello, sb);
        Assert.assertEquals("\"Hello\"", sb.toString());
    }

    @Test
    public void enumAsClassTest() throws Exception {
        ConfInfo.setEnumAsClass(true);
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(TestEnum.Hello, sb);
        TestEnum en = JsonReader.read(sb.toString(), TestEnum.class);
        Assert.assertNotNull("Shouldn't be null", en);
        Assert.assertEquals(TestEnum.Hello, en);
    }

    @Test
    public void enumNoClassTest() throws Exception {
        ConfInfo.setEnumAsClass(false);
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(TestEnum.Hello, sb);
        Assert.assertEquals("\"Hello\"", sb.toString());
    }

    @Test
    public void listInObjectTest() throws Exception {
        listObj = new ListObj();
        listObj.str = "listInObjectTest";
        listObj.list = Arrays.asList(1,5,3,4, 0);
        JsonWriter.write(listObj, sb);
        assertSplitSimpleJson("{\"list\":[1,5,3,4,0],\"str\":\"listInObjectTest\",\"class\":\"com.mikerusoft.jsonable.JsonTest$ListObj\"}", sb.toString());
        ListObj another = JsonReader.read(sb.toString(), ListObj.class);
        assertNotNull(another);
        assertEquals(listObj.str, another.str);
        assertArrayEquals(listObj.list.toArray(), another.list.toArray());
    }

    @Test
    public void simpleAdapterTest() throws Exception {
        ConfInfo.registerAdapter(SimpleObjNoAnot.class, new String[] {"str", "num", "bool"});
        simpleNoAnot.bool = true;
        simpleNoAnot.str = "Hello";
        simpleNoAnot.num = 10;
        ConfInfo.setExcludeClass(true);
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(simpleNoAnot, sb);
        assertSplitSimpleJson("{\"str\":\"Hello\",\"bool\":true,\"num\":10}", sb.toString());
        ConfInfo.setExcludeClass(false);
        sb = new StringBuilder();
        JsonWriter.write(simpleNoAnot, sb);
        assertSplitSimpleJson("{\"str\":\"Hello\",\"bool\":true,\"num\":10,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjNoAnot\"}", sb.toString());
        SimpleObjNoAnot test = JsonReader.read(sb.toString(), SimpleObjNoAnot.class);
        assertEquals(simpleNoAnot, test);
        ConfInfo.unset();
        ConfInfo.registerAdapter(SimpleObjNoAnot.class, new String[] {"str", "num"});
        sb = new StringBuilder();
        JsonWriter.write(simpleNoAnot, sb);
        assertSplitSimpleJson("{\"str\":\"Hello\",\"num\":10,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjNoAnot\"}", sb.toString());
        ConfInfo.setExcludeClass(false);
        sb = new StringBuilder();
        JsonWriter.write(simpleNoAnot, sb);
        assertSplitSimpleJson("{\"str\":\"Hello\",\"num\":10,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjNoAnot\"}", sb.toString());
        ConfInfo.unset();
        ConfInfo.registerAdapter(new SimpleBeanAdapter<>(SimpleObjNoAnot.class));
        ConfInfo.setExcludeClass(true);
        sb = new StringBuilder();
        JsonWriter.write(simpleNoAnot, sb);
        assertSplitSimpleJson("{\"str\":\"Hello\",\"bool\":true,\"num\":10}", sb.toString());
        ConfInfo.setExcludeClass(false);
        sb = new StringBuilder();
        JsonWriter.write(simpleNoAnot, sb);
        assertSplitSimpleJson("{\"str\":\"Hello\",\"bool\":true,\"num\":10,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjNoAnot\"}", sb.toString());
    }

    @Test
    public void objectInObjectTest() throws Exception {
        ConfInfo.registerAdapter(SimpleObjNoAnot.class, new String[] {"str", "num", "bool"});
        ConfInfo.registerAdapter(new SimpleBeanAdapter<>(Pair.class));
        ConfInfo.setExcludeClass(false);
        ConfInfo.setIncludePrimitiveClass(true);
        Pair<SimpleObjAnnot, SimpleObjNoAnot> p = new Pair<>(new SimpleObjAnnot(), new SimpleObjNoAnot());
        p.getLeft().num = 33;
        p.getLeft().str1 = "Left";
        p.getRight().bool = false;
        p.getRight().str = "Right";
        p.getRight().num = Integer.MAX_VALUE;
        ByteArrayOutputStream byos = new ByteArrayOutputStream();
        JsonWriter.write(p, byos, "UTF-8");
        String result = new String(byos.toByteArray());
        Pair<SimpleObjAnnot, SimpleObjNoAnot> p1 = JsonReader.read(new ByteArrayInputStream(result.getBytes("UTF-8")), Pair.class);
        Assert.assertEquals(p, p1);
    }

    @Test
    public void objectInObjectPropertyTest() throws Exception {
        ConfInfo.registerAdapter(SimpleObjNoAnot.class, new PropertyPair[] { new PropertyPair("str", "str"), new PropertyPair("num", "num"), new PropertyPair("bool", "bool")});
        ConfInfo.registerAdapter(new SimpleBeanAdapter<>(Pair.class));
        ConfInfo.setExcludeClass(false);
        ConfInfo.setIncludePrimitiveClass(true);
        Pair<SimpleObjAnnot, SimpleObjNoAnot> p = new Pair<>(new SimpleObjAnnot(), new SimpleObjNoAnot());
        p.getLeft().num = 33;
        p.getLeft().str1 = "Left";
        p.getRight().bool = false;
        p.getRight().str = "Right";
        p.getRight().num = Integer.MAX_VALUE;
        ByteArrayOutputStream byos = new ByteArrayOutputStream();
        JsonWriter.write(p, byos, "UTF-8");
        String result = new String(byos.toByteArray());
        Pair<SimpleObjAnnot, SimpleObjNoAnot> p1 = JsonReader.read(new ByteArrayInputStream(result.getBytes("UTF-8")), Pair.class);
        Assert.assertEquals(p, p1);
    }

    @Test
    public void mapStartingWithNullTest() throws Exception {
        Map t = new HashMap();
        t.put("a", null);
        t.put("z", "Hello");
        JsonWriter.write(t, sb);
        Assert.assertEquals("{\"z\":\"Hello\"}", sb.toString());
    }

    @Test
    public void multipleListTest() throws Exception {
        String str = "[[[\"hello\", \"bye\"]], [\"1\", \"2\"]]";
        List<?> lst = JsonReader.read(str, List.class);
        Assert.assertNotNull(lst);
        Assert.assertNotNull(lst.get(0));
        Assert.assertTrue(lst.get(0) instanceof List);
        List<?> subList = (List<?>)lst.get(0);
        Assert.assertNotNull(subList.get(0));
        Assert.assertTrue(subList.get(0) instanceof List);
    }

    @Test
    public void objectStartingWithNullTest() throws Exception {
        SimpleObjAnnot t = new SimpleObjAnnot();
        t.num = 1;
        t.str1 = null;
        ConfInfo.setExcludeClass(true);
        JsonWriter.write(t, sb);
        Assert.assertEquals("{\"num\":1}", sb.toString());
    }

    @Test
    public void readFactoryTest() {
        ConfInfo.setExcludeClass(false);
        ConfInfo.registerFactories(new ReadInstanceFactory<SimpleObjAnnot, SimpleObjAnnotExtend>() {

            @Override
            public Class<SimpleObjAnnot> getFactoryClass() {
                return SimpleObjAnnot.class;
            }

            @Override
            public SimpleObjAnnotExtend newInstance(Map<String, Object> data) throws IllegalAccessException, InstantiationException {
                return new SimpleObjAnnotExtend();
            }
        });
        SimpleObjAnnot soa = null;
        try {
            soa = JsonReader.read("{\"str\":\"Hello\",\"num\":1,\"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnot\"}", SimpleObjAnnot.class);
        } catch (Exception ignore){ ignore.printStackTrace(); }
        Assert.assertNotNull(soa);
        Assert.assertTrue(soa instanceof SimpleObjAnnotExtend);

        soa = null;
        try {
            soa = JsonReader.read("{\"str\":\"Hello\",\"num\":1,\"extended\":\"NewValue\", \"class\":\"com.mikerusoft.jsonable.JsonTest$SimpleObjAnnot\"}", SimpleObjAnnot.class);
        } catch (Exception ignore){}
        Assert.assertNotNull(soa);
        Assert.assertTrue(soa instanceof SimpleObjAnnotExtend);
        Assert.assertEquals("NewValue", ((SimpleObjAnnotExtend)soa).extended);
    }

    private static void assertSplitSimpleJson(String expected, String actual) {
        assertSplitSimpleJson(null, expected, actual);
    }

    private static void assertSplitSimpleJson(String msg, String expected, String actual) {
        String[] expAr = StringUtils.split(expected.substring(1, expected.length() - 1), ",");
        Arrays.sort(expAr);
        String[] actAr = StringUtils.split(actual.substring(1, actual.length() - 1), ",");
        Arrays.sort(actAr);
        assertArrayEquals(msg, expAr, actAr);
    }
}
