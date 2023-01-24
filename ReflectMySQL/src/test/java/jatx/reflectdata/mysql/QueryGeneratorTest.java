package jatx.reflectdata.mysql;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.willdom.botsystem.backend.status.BotInfo;
import com.willdom.botsystem.backend.status.BotStatus;
import com.willdom.botsystem.backend.status.ProcessedEntity;

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
    public void generateCreateBotInfoCode() throws MoreThanOnePrimaryKeyException, IllegalAccessException, NotNullColumnHasNullValueException {
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

    @Test
    public void generateProcessedEntityCode() throws MoreThanOnePrimaryKeyException, IllegalAccessException, NotNullColumnHasNullValueException {
        QueryGenerator generator = new QueryGenerator("source.aut_bots_last_processed", ProcessedEntity.class, 1, false);
        String createTableQuery = generator.generateCreateTableQuery();
        System.out.println(createTableQuery);
        System.out.println("------------");
        
        ProcessedEntity processedEntity = new ProcessedEntity("nelson", "profile", "www.noone.com", "empty result", "542367", new Timestamp(System.currentTimeMillis()));
        String insertQuery = generator.generateInsertQuery(processedEntity);
        System.out.println(insertQuery);
        System.out.println("------------");
        
        String insertCode = generator.generateInsertCode(processedEntity);
        System.out.println(insertCode);
        System.out.println("------------");
    }

    @Test
    public void generateBotStatusCode() throws MoreThanOnePrimaryKeyException, IllegalAccessException, NotNullColumnHasNullValueException {
        QueryGenerator generator = new QueryGenerator("source.aut_bots_status", BotStatus.class, 1, false);
        String createTableQuery = generator.generateCreateTableQuery();
        System.out.println(createTableQuery);
        System.out.println("------------");
        
        ProcessedEntity processedEntity = new ProcessedEntity("nelson", "profile", "www.noone.com", "empty result", "542367", new Timestamp(System.currentTimeMillis()));
        BotStatus status = new BotStatus("nelson", "online", new Timestamp(System.currentTimeMillis()), 700L, processedEntity);
        
        String insertQuery = generator.generateInsertQuery(status);
        System.out.println(insertQuery);
        System.out.println("------------");
        
        String insertCode = generator.generateInsertCode(status);
        System.out.println(insertCode);
        System.out.println("------------");
    }


}
