package de.zalando.sprocwrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import javax.validation.ConstraintViolationException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

import de.zalando.sprocwrapper.example.AddressPojo;
import de.zalando.sprocwrapper.example.ExampleDomainObject;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithDate;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithEnum;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithInnerObject;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithMap;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithRandomFields;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithRandomFieldsInner;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithRandomFieldsOverride;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithSimpleTransformer;
import de.zalando.sprocwrapper.example.ExampleDomainObjectWithValidation;
import de.zalando.sprocwrapper.example.ExampleEnum;
import de.zalando.sprocwrapper.example.ExampleNamespacedSProcService;
import de.zalando.sprocwrapper.example.ExampleSProcService;
import de.zalando.sprocwrapper.example.ExampleValidationSProcService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:backendContextTest.xml"})
public class SimpleIT {

    @Autowired
    private ExampleSProcService exampleSProcService;

    @Autowired
    private ExampleValidationSProcService exampleValidationSProcService;

    @Autowired
    private ExampleNamespacedSProcService exampleNamespacedSProcService;

    @Autowired
    @Qualifier("testDataSource1")
    private DataSource dataSource1;

    @Test
    public void testSample() throws SQLException {

        // test void result
        exampleSProcService.getSimpleIntVoid(1);

        assertEquals(3, (int) exampleSProcService.getSimpleInt());
        assertEquals(3, exampleSProcService.getSimpleIntAsPrimitive());
        exampleSProcService.createArticleSimpleItems("sku", 1, 12, 13, "1001");

        assertEquals(true, exampleSProcService.getBoolean());

        exampleSProcService.setBoolean(true);
    }

    @Test
    public void testSimpleTransformer() throws SQLException {

        // test void result
        final ExampleDomainObjectWithSimpleTransformer transformed = exampleSProcService.testSimpleTransformer(
                new ExampleDomainObjectWithSimpleTransformer("123", "hallo"));

        assertEquals("123", transformed.getA());
        assertEquals("hallo", transformed.getB());
    }

    @Test
    public void testSimpleListParam() throws SQLException {

        final List<String> skus = new ArrayList<String>();
        skus.add("ABC123");
        skus.add("ABC456");

        exampleSProcService.createArticleSimples(skus);
    }

    @Test
    public void testMultiRowTypeMappedResult() {

        // Query for a Multi Row Resultset of TestResult Objects
        final List<ExampleDomainObject> rows = exampleSProcService.getResult();
        assertEquals("a", rows.get(0).getA());
        assertEquals("b", rows.get(0).getB());
        assertEquals("c", rows.get(1).getA());
        assertEquals("d", rows.get(1).getB());
    }

    @Test
    public void testParameterOverloading() {
        assertEquals(3, (int) exampleSProcService.getSimpleInt());
        assertEquals(1234, exampleSProcService.getSimpleInt(1234));
    }

    @Test
    public void testObjectParam() {

        String result = exampleSProcService.createOrUpdateObject(null);
        assertEquals(null, result);

        final ExampleDomainObject obj = new ExampleDomainObject("a", "b");
        result = exampleSProcService.createOrUpdateObject(obj);
        assertEquals("a b", result);
    }

    @Test
    public void testListParam() {

        String result = exampleSProcService.createOrUpdateMultipleObjects(null);
        assertEquals("", result);

        result = exampleSProcService.createOrUpdateMultipleObjects(new ArrayList<ExampleDomainObject>());
        assertEquals("", result);

        final ExampleDomainObject obj = new ExampleDomainObject("a", "b");
        final List<ExampleDomainObject> list = new ArrayList<ExampleDomainObject>();
        list.add(obj);
        list.add(new ExampleDomainObject("c", "d"));

        result = exampleSProcService.createOrUpdateMultipleObjects(list);
        assertEquals("a_b,c_d,", result);
    }

    @Test
    public void testListParamWithMap() {

        String result = exampleSProcService.createOrUpdateMultipleObjectsWithMap(null);
        assertNull(result);

        result = exampleSProcService.createOrUpdateMultipleObjectsWithMap(new ArrayList<ExampleDomainObjectWithMap>());
        assertNull(result);

        final ExampleDomainObjectWithMap obj = new ExampleDomainObjectWithMap("a", null);
        final List<ExampleDomainObjectWithMap> list = new ArrayList<ExampleDomainObjectWithMap>();
        list.add(obj);
        list.add(new ExampleDomainObjectWithMap("c", new HashMap<String, String>()));
        list.get(1).b.put("key", "val");

        result = exampleSProcService.createOrUpdateMultipleObjectsWithMap(list);
        assertEquals("<c_key_val>", result);

        list.get(0).b = new HashMap<String, String>();

        result = exampleSProcService.createOrUpdateMultipleObjectsWithMap(list);
        assertEquals("<a__>,<c_key_val>", result);

        // test void result
        exampleSProcService.createOrUpdateMultipleObjectsWithMapVoid(list);
    }

    @Test
    public void textComplexParam() {

        String result = exampleSProcService.createOrUpdateMultipleObjectsWithInnerObject(null);
        assertNull(result);

        result = exampleSProcService.createOrUpdateMultipleObjectsWithInnerObject(
                new ArrayList<ExampleDomainObjectWithInnerObject>());
        assertNull(result);

        final ExampleDomainObjectWithInnerObject obj = new ExampleDomainObjectWithInnerObject("a", null);
        final List<ExampleDomainObjectWithInnerObject> list = new ArrayList<ExampleDomainObjectWithInnerObject>();
        list.add(obj);
        list.add(new ExampleDomainObjectWithInnerObject("c", new ExampleDomainObject("d", "e")));

        result = exampleSProcService.createOrUpdateMultipleObjectsWithInnerObject(list);
        assertEquals("<c_d|e>", result);

        obj.setC(new ArrayList<ExampleDomainObject>());
        obj.getC().add(new ExampleDomainObject("f", "g"));
        result = exampleSProcService.createOrUpdateMultipleObjectsWithInnerObject(list);
        assertEquals("<c_d|e>", result);
    }

    @Test
    public void testEnum() {
        exampleSProcService.useEnumParam(ExampleEnum.ENUM_CONST_1);

        // exampleSProcService.createOrUpdateObjectWithEnum(null);

        final ExampleDomainObjectWithEnum obj = new ExampleDomainObjectWithEnum();
        obj.setX("X");
        obj.setMyEnum(ExampleEnum.ENUM_CONST_1);

        final String result = exampleSProcService.createOrUpdateObjectWithEnum(obj);
        assertEquals("XENUM_CONST_1", result);
    }

    @Test
    public void testReturnDomainObjectWithEnum() {
        final ExampleDomainObjectWithEnum obj = exampleSProcService.getEntityWithEnum(1L);
        Assert.assertNotNull(obj);
        Assert.assertEquals("sample x", obj.getX());
        Assert.assertEquals(ExampleEnum.ENUM_CONST_1, obj.getMyEnum());
    }

    @Test
    public void testDate() {
        exampleSProcService.useDateParam(null);
        exampleSProcService.useDateParam(new Date(System.currentTimeMillis()));

        // commented out, because date input parameters are not working at the moment
        // exampleSProcService.useDateParam2(new Date(System.currentTimeMillis()));

        final ExampleDomainObjectWithDate obj = new ExampleDomainObjectWithDate();
        obj.setX("X");

        String result = exampleSProcService.createOrUpdateObjectWithDate(obj);
        assertNull(result);

        final Date d = new Date(System.currentTimeMillis());
        obj.setMyDate(d);
        result = exampleSProcService.createOrUpdateObjectWithDate(obj);
        assertEquals("X" + (new SimpleDateFormat("yyyy-MM-dd").format(d)), result);

        obj.setMyTimestamp(d);
        result = exampleSProcService.createOrUpdateObjectWithDate(obj);
        assertEquals("X" + (new SimpleDateFormat("yyyy-MM-dd").format(d))
                + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d)), result);
    }

    @Test
    public void testChar() {
        exampleSProcService.useCharParam('m');
    }

    @Test
    public void testReturnDate() {
        final Date d = exampleSProcService.getFixedTestDate();
        System.out.println("Date d:" + d);
        System.out.println("Date now:" + (new Date(System.currentTimeMillis())));

        System.out.println(d.getClass().getName());

        assertEquals(1328266821000L, d.getTime()); // extract(epoch from '2012-02-03 12:00:21'::timestamp)*1000
    }

    @Test
    public void testIntegerListParam() {
        exampleSProcService.useIntegerListParam(Lists.newArrayList(1, 2));
    }

    @Test
    public void testCreateAddress() {
        final AddressPojo a = getNewTestAddress();

        final AddressPojo b = exampleSProcService.createAddress(a);
        assertEquals((int) b.id, 1);

        final AddressPojo c = exampleSProcService.createAddress(a);
        assertEquals((int) c.id, 2);
    }

    @Test
    public void testGetAddress() {
        final AddressPojo a = getNewTestAddress();

        final AddressPojo b = exampleSProcService.createAddress(a);

        final AddressPojo c = new AddressPojo();
        c.id = b.getId();

        final AddressPojo l = exampleSProcService.getAddress(c);
        System.out.println(l);

        assertEquals(l.customerId, a.customerId);
        assertEquals(l.number, a.number);
        assertEquals(l.street, a.street);

        System.out.println(l);
    }

    private static int addresscount = 1;

    private AddressPojo getNewTestAddress() {
        final AddressPojo a = new AddressPojo();
        a.customerId = addresscount++;
        a.street = "Auf Beverau";
        a.number = "11";
        return a;
    }

    @Test
    public void testGetAddressSql() {

        final AddressPojo a = getNewTestAddress();

        final AddressPojo b = exampleSProcService.createAddress(a);

        final AddressPojo c = new AddressPojo();
        c.id = b.getId();

        final AddressPojo l = exampleSProcService.getAddress(c);
        System.out.println(l);

        assertEquals(l.customerId, a.customerId);
        assertEquals(l.number, a.number);
        assertEquals(l.street, a.street);

        System.out.println(l);
    }

    @Test
    public void testSensitiveParameter() {

        // password should not be logged to logfile!
        exampleSProcService.login("henning.jacobs", "mySecR3tPassW0rd");
    }

    @Test
    public void testNamespacedService() {
        assertEquals("TESTRESULT", exampleNamespacedSProcService.test());
    }

    @Test
    public void testPrimitiveListResults() {
        final List<Integer> ints = exampleSProcService.getInts();
        assertEquals(2, ints.size());
        assertEquals(1, (int) ints.get(0));
        assertEquals(2, (int) ints.get(1));

        final List<Long> longs = exampleSProcService.getLongs();
        assertEquals(2, longs.size());
        assertEquals(1000, (long) longs.get(0));
        assertEquals(2002, (long) longs.get(1));
    }

    /**
     * test correct mapping of complex types with inner type and random field ordering (i.e. not alphabetically sorted)
     */
    @Test
    public void textComplexParamNameMapping() {

        String result = exampleSProcService.createOrUpdateObjectWithRandomFields(null);
        assertNull(result);

        final ExampleDomainObjectWithRandomFields obj = new ExampleDomainObjectWithRandomFields();
        obj.setX("X");
        obj.setY("Y");
        obj.setZ(3);
        obj.setInnerObject(new ExampleDomainObjectWithRandomFieldsInner("x", "y", "z"));
        obj.setList(Lists.newArrayList(new ExampleDomainObjectWithRandomFieldsInner("a", "b", "c")));
        result = exampleSProcService.createOrUpdateObjectWithRandomFields(obj);

        // check that field ordering is correct
        assertEquals("XY3xyz(<abc>)", result);

        result = exampleSProcService.createOrUpdateMultipleObjectsWithRandomFields(Lists.newArrayList(
                    new ExampleDomainObjectWithRandomFields("X", "Y", 1)));
        assertEquals("XY1", result);
    }

    /**
     * test correct mapping of complex types with inner type and random field ordering (i.e. not alphabetically sorted)
     */
    @Test
    public void textComplexParamNameMappingNoAnnotation() {

        String result;

        result = exampleSProcService.createOrUpdateMultipleObjectsWithRandomFieldsNoAnnotation(Lists.newArrayList(
                    new ExampleDomainObjectWithRandomFields("X", "Y", 1)));
        assertEquals("XY1", result);
    }

    /**
     * Test override of database type in domain objects passed in lists.
     */
    @Test
    public void textComplexParamNameMappingNoAnnotationOverride() {

        String result;

        result = exampleSProcService.createOrUpdateMultipleObjectsWithRandomFieldsNoAnnotationOverride(Lists
                    .newArrayList(new ExampleDomainObjectWithRandomFieldsOverride("X", "Y", 1)));
        assertEquals("XY1", result);
    }

    @Test
    @Ignore("performance test only")
    public void testRuntime() {
        assertEquals(1, 1);

        final int loops = 10000;

        final String sql = "SELECT ";

        final int xx = (new JdbcTemplate(dataSource1)).queryForInt(sql + 11111);

        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            final int j = (new JdbcTemplate(dataSource1)).queryForInt(sql + i);
        }

        final long endTime = System.currentTimeMillis();

        final long startTimeW = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            final int j = exampleSProcService.getSimpleInt(i);
        }

        final long endTimeW = System.currentTimeMillis();

        final long startTimeN = System.currentTimeMillis();

        for (int i = 0; i < loops; i++) {
            Connection conn = null;
            try {
                conn = dataSource1.getConnection();

                final Statement st = conn.createStatement();

                int j = 0;

                final ResultSet rs = st.executeQuery("SELECT " + i);

                if (rs.next()) {
                    j = rs.getInt(1);
                }

            } catch (final SQLException e) { }
            finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (final SQLException e) { }
                }
            }
        }

        final long endTimeN = System.currentTimeMillis();

        System.out.println("Time used for native JdbcTemplate: " + (endTime - startTime));
        System.out.println("Time used for SprocWrapper: " + (endTimeW - startTimeW));
        System.out.println("Time used for Native: " + (endTimeN - startTimeN));
    }

    @Test
    public void testTimeout() {
        final String timeout = exampleSProcService.showTimeout();

        exampleSProcService.testTimeoutSetTo3s(2);

        try {
            exampleSProcService.testTimeoutSetTo3s(4);
            assertEquals(true, false);
        } catch (final Exception e) {
            assertEquals(true, true);
        }

        final String timeout2 = exampleSProcService.showTimeout();
        assertEquals(timeout, timeout2);

        try {
            exampleSProcService.testTimeoutSetTo5s(6);
            assertEquals(true, false);
        } catch (final Exception e) {
            assertEquals(true, true);
        }

        final String timeout3 = exampleSProcService.showTimeout();
        assertEquals(timeout, timeout3);
    }

    @Test
    public void testNullObject() {
        final ExampleDomainObjectWithInnerObject obj = exampleSProcService.getObjectWithNull();

        assertEquals(null, obj.getC());
    }

    @Test
    public void testValidValidation1() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation("test", 4);

        exampleSProcService.testSprocCallWithoutValidation1(obj);
        exampleSProcService.testSprocCallWithoutValidation2(obj);
        exampleSProcService.testSprocCallWithValidation(obj);

        exampleValidationSProcService.testSprocCallWithoutValidation(obj);
        exampleValidationSProcService.testSprocCallWithValidation1(obj);
        exampleValidationSProcService.testSprocCallWithValidation2(obj);
        exampleValidationSProcService.testSprocCallWithValidation3(obj);
    }

    @Test
    public void testValidValidation2() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation("test", 1);
        exampleSProcService.testSprocCallWithoutValidation1(obj);
        exampleSProcService.testSprocCallWithoutValidation2(obj);
        exampleValidationSProcService.testSprocCallWithoutValidation(obj);
    }

    @Test
    public void testValidValidation3() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation(null, null);
        exampleSProcService.testSprocCallWithoutValidation1(obj);
        exampleSProcService.testSprocCallWithoutValidation2(obj);
        exampleValidationSProcService.testSprocCallWithoutValidation(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidation4() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation("test", 1);
        exampleSProcService.testSprocCallWithValidation(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidation5() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation("test", 1);
        exampleValidationSProcService.testSprocCallWithValidation1(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidation6() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation("test", 1);
        exampleValidationSProcService.testSprocCallWithValidation2(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidation7() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation("test", 1);
        exampleValidationSProcService.testSprocCallWithValidation3(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidation8() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation(null, null);
        exampleSProcService.testSprocCallWithValidation(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidation9() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation(null, null);
        exampleValidationSProcService.testSprocCallWithValidation1(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidation10() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation(null, null);
        exampleValidationSProcService.testSprocCallWithValidation2(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidation11() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation(null, null);
        exampleValidationSProcService.testSprocCallWithValidation3(obj);
    }

    @Test
    public void testValidValidationReturnValue1() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation("test", 4);
        exampleSProcService.testSprocCallWithValidationInvalidRet1(obj);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testValidValidationReturnValue2() {
        final ExampleDomainObjectWithValidation obj = new ExampleDomainObjectWithValidation("test", 4);
        exampleSProcService.testSprocCallWithValidationInvalidRet2(obj);
    }
}
