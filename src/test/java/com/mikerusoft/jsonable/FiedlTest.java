package com.mikerusoft.jsonable;

import com.mikerusoft.jsonable.parser.JsonReader;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import telemessage.web.services.UserResponse;

import java.util.List;

public class FiedlTest {

    @Test
    public void testParse() throws Exception {
        List<UserResponse> urs = (List<UserResponse>)JsonReader.read(FiedlTest.class.getClassLoader().getSystemResource("FieldTest.json").openStream(), UserResponse.class);
        Assert.assertNotNull(urs);
        Assert.assertEquals(1, urs.size());
        UserResponse resp = urs.get(0);
        Assert.assertNotNull(resp);
        Assert.assertEquals(0, resp.getResultCode());
    }

}
