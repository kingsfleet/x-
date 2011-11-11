package com.kingsfleet.simple.x;

import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestX {
    @Test
    public void simpleRead() {

        X x = getSimpleTestData();

        String elementValue = x.get("n:elementValue");
        assertEquals("ElementValue", elementValue);
        String attributeValue = x.get("n:attributeValue/@attr");
        assertEquals("AttributeValue", attributeValue);

        x.set("n:elementValue", "Fish");
        x.set("n:attributeValue/@attr", "Fish");

        elementValue = x.get("n:elementValue");
        assertEquals("Fish", elementValue);
        attributeValue = x.get("n:attributeValue/@attr");
        assertEquals("Fish", attributeValue);

    }

    @Test
    public void simpleAddChildElement() {
        X x = getSimpleTestData();
        X child = x.children().create("textst");

        assertEquals(
            "Added path",
            "textst<-simple",
            child.toString());
        
        assertTrue(
            "dumped xml should contain the new element",
            dumpToString(x).contains("textst"));
        
        assertNotNull(
            "Should be able to find in the children",
            x.select("n:textst"));
    }


    @Test
    public void setAttributeThatIsntThere() {
        X x = getSimpleTestData();

        x.set("@newAttribute", "NewAttribute");
        

        assertEquals(
            "Added path",
            "NewAttribute",
            x.get("@newAttribute"));
        
        assertTrue(
            "dumped xml should contain the new element",
            dumpToString(x).contains("newAttribute=\""));
    }


    @Test
    public void testWritingOut() {
    
        X x = getSimpleTestData();

        String dumpToString = dumpToString(x);
        assertTrue(
            "dumped xml should contain the root element", 
            dumpToString.contains("simple"));

    }








    /**
     * @return dump x to a string
     */
    private String dumpToString(X x) {
        StringWriter sw = new StringWriter();
        x.out(sw);
        return sw.toString();
    }

    /**
     * @return An X with very simple test data in it
     */
    private X getSimpleTestData() {
        X x = X.in(TestX.class.getResourceAsStream("verySimpleTest.xml"));
        return x;
    }
}
