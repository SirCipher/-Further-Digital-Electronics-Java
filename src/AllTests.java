import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ client.MEMEClientTest.class, server.XMLReaderTest.class, server.MEMEServerTest.class})

public class AllTests {
	
	/*
	 * This class automatically runs all three tests, in the following order;
	 * 
	 * -the XML reader
	 * -the Server
	 * -the Client
	 */
	
}