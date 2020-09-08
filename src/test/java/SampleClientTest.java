import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class SampleClientTest {

	@Test
	public void shouldRunSearch() throws Exception {
		IGenericClient spyHapiClient = spy(SampleClient.context.newRestfulGenericClient("http://hapi.fhir.org/baseR4"));
		SampleClient client = spy(new SampleClient(spyHapiClient));
		try {
			client.search();
		} catch (IOException e) {
			fail("Unable to run search");
		}
		
		// check we ran search 3 times, one time with caching off
		verify(client, times(2)).runSearch(any(), eq(false));
		verify(client, times(1)).runSearch(any(), eq(true));
		
		// check we invoked client 3 times for each name
		verify(spyHapiClient, times(3 * 20)).search();
	}

}
