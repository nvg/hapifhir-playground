import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.util.StopWatch;

public class TimingInterceptorTest {

	@Test
	public void shouldCountZero() {
		TimingInterceptor ti = new TimingInterceptor();
		ti.clear();

		assertEquals(0, ti.getAverageResponseTime(), 0.00001);
	}

	@Test
	public void shouldCountAverage() {
		TimingInterceptor ti = new TimingInterceptor();
		ti.clear();

		StopWatch stopWatch = mock(StopWatch.class);
		when(stopWatch.getMillis()).thenReturn(1000l);

		IHttpResponse response = mock(IHttpResponse.class);
		when(response.getRequestStopWatch()).thenReturn(stopWatch);

		ti.interceptResponse(response);
		ti.interceptResponse(response);

		assertEquals(1000, ti.getAverageResponseTime(), 0.00001);
	}

}
