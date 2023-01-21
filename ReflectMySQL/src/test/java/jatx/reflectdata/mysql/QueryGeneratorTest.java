package jatx.reflectdata.mysql;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.willdom.botsystem.backend.status.BotInfo;

import jatx.reflectdata.mysql.ReflectSQLExceptions.MoreThanOnePrimaryKeyException;
import jatx.reflectdata.mysql.ReflectSQLExceptions.NotNullColumnHasNullValueException;

public class QueryGeneratorTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGenerateCreateTableQuery() throws MoreThanOnePrimaryKeyException, IllegalAccessException, NotNullColumnHasNullValueException {
        QueryGenerator generator = new QueryGenerator("source.aut_bots_info", BotInfo.class, 1, false);
        String createTableQuery = generator.generateCreateTableQuery();
        System.out.println(createTableQuery);
        System.out.println("------------");
        
        BotInfo botInfo = new BotInfo("testbot", "Test Bot 1", "development-01", "oppscout");
        String insertQuery = generator.generateInsertQuery(botInfo);
        System.out.println(insertQuery);
        System.out.println("------------");
        
        String insertCode = generator.generateInsertCode(botInfo);
        System.out.println(insertCode);
        System.out.println("------------");

    }

}
