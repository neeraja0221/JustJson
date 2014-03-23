/*
 * Copyright (C) 2014 Kalin Maldzhanski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.djodjo.json.test;


import junit.framework.Assert;

import org.djodjo.json.JsonElement;
import org.djodjo.json.JsonObject;
import org.djodjo.json.JsonReader;
import org.djodjo.json.Validator;
import org.djodjo.json.schema.Schema;
import org.djodjo.json.schema.SchemaV4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URI;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class JsonSchemaV4Validation {
    Schema schema;
    JsonObject jsonObject;

    @Before
    public void setUp() throws Exception {
        //create an empty v4 schema http://json-schema.org/draft-04/schema#
        schema =  new SchemaV4();
        //create an object to validate
        jsonObject = JsonObject.readFrom("{ " +
                "\"obj\":{}," +
                "\"arr\":[]," +
                "\"num\":5," +
                "\"str\":\"123456\"," +
                "\"bool\": true," +
                "\"null\": null" +
                "}").asJsonObject();
    }

    @Test
    public void testValidatesAll() throws Exception {
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("obj")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("arr")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("num")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("str")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("bool")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("null")));
    }

    @Test
    public void testTypeObject() throws Exception {
        schema.wrap(JsonElement.readFrom("{\"type\" : \"object\"}"));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("obj")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("arr")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("num")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("str")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("bool")));
    }
    @Test
    public void testTypeArray() throws Exception {
        schema.wrap(JsonElement.readFrom("{\"type\" : \"array\"}"));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("obj")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("arr")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("num")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("str")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("bool")));
    }
    @Test
    public void testTypeString() throws Exception {
        schema.wrap(JsonElement.readFrom("{\"type\" : \"string\"}"));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("obj")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("arr")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("num")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("str")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("bool")));
    }
    @Test
    public void testTypeNumber() throws Exception {
        schema.wrap(JsonElement.readFrom("{\"type\" : \"number\"}"));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("obj")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("arr")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("num")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("str")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("bool")));
    }
    @Test
    public void testTypeBool() throws Exception {
        schema.wrap(JsonElement.readFrom("{\"type\" : \"boolean\"}"));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("obj")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("arr")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("num")));
        assertFalse(schema.getDefaultValidator().isValid(jsonObject.get("str")));
        assertTrue(schema.getDefaultValidator().isValid(jsonObject.get("bool")));
    }

    @Test
    public void testProperties() throws Exception {
        schema.wrap(JsonElement.readFrom("{" +
                "\"type\" : \"object\"," +
                "\"properties\" : {" +
                "\"one\" : {\"type\" : \"string\"  } ," +
                "\"two\" : {\"type\" : \"object\" }" +
                "}" +
                "}"));
        Validator validator = schema.getDefaultValidator();
        assertFalse(validator.isValid(JsonElement.readFrom("{ " +
                "\"one\":\"string is good\"," +
                "\"two\":[]" +
                "}")));

        assertFalse(validator.isValid(JsonElement.readFrom("{ " +
                "\"two\":\"string is bad\"," +
                "\"one\":{}" +
                "}")));

        assertTrue(validator.isValid(JsonElement.readFrom("{ " +
                "\"one\":\"string is good\"" +
                "}")));

        assertTrue(validator.isValid(JsonElement.readFrom("{ " +
                "\"two\":{}" +
                "}")));

        assertTrue(validator.isValid(JsonElement.readFrom("{ " +
                "\"one\":\"string is good\"," +
                "\"two\":{}" +
                "}")));
    }

    @Test
    public void testRequired() throws Exception {
        schema.wrap(JsonElement.readFrom("{\"required\" : [\"other\"]}"));
        Validator validator = schema.getDefaultValidator();
        assertFalse(validator.isValid(jsonObject));
        schema.wrap(JsonElement.readFrom("{\"required\" : [\"obj\"]}"));
        validator = schema.getDefaultValidator();
        assertTrue(validator.isValid(jsonObject));
        schema.wrap(JsonElement.readFrom("{\"required\" : [\"obj\",\"another\"]}"));
        validator = schema.getDefaultValidator();
        assertFalse(validator.isValid(jsonObject));
        schema.wrap(JsonElement.readFrom("{\"required\" : [\"obj\",\"arr\"]}"));
        validator = schema.getDefaultValidator();
        assertTrue(validator.isValid(jsonObject));


    }

    @Test
    public void testMultipleOf() throws Exception {
        schema.wrap(JsonElement.readFrom("{" +
                "\"type\" : \"object\"," +
                "\"properties\" : {" +
                "\"one\" : {\"type\" : \"number\"   ," +
                "\"multipleOf\" : 5 }" +
                "}" +
                "}"));
        Validator validator = schema.getDefaultValidator();
        assertFalse(validator.isValid(JsonElement.readFrom("{ " +
                "\"one\":1" +
                "}")));
        assertTrue(validator.isValid(JsonElement.readFrom("{ " +
                "\"one\":10" +
                "}")));
        schema.wrap(JsonElement.readFrom("{" +
                "\"type\" : \"object\"," +
                "\"properties\" : {" +
                "\"one\" : {\"type\" : \"number\"   ," +
                "\"multipleOf\" : 3.4 }" +
                "}" +
                "}"));
        validator = schema.getDefaultValidator();
        assertFalse(validator.isValid(JsonElement.readFrom("{ " +
                "\"one\":1" +
                "}")));
        assertTrue(validator.isValid(JsonElement.readFrom("{ " +
                "\"one\":6.8" +
                "}")));
    }
}
