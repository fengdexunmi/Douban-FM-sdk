package com.zzxhdzj.douban.api.channels.genre;

import com.zzxhdzj.douban.api.BaseGatewayTestCase;
import com.zzxhdzj.douban.api.mock.TestResponses;
import com.zzxhdzj.http.ApiRequest;
import com.zzxhdzj.http.Callback;
import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: yangning.roy
 * Date: 11/28/13
 * Time: 12:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenreChannelsGatewayTest extends BaseGatewayTestCase {
    private int genreId;
    private GenreChannelGateway genreChannelGateway;
    private int start;
    private int limit;

    @Before
    public void setUp() {
        super.setUp();
        start = 1;
        limit = 1;
        genreId = 335;
        genreChannelGateway = new GenreChannelGateway(douban, apiGateway);
    }

    @Test
    public void shouldFetchOneChannelBySpecificGenre() throws Exception {
        genreChannelGateway.fetchChannelsByGenreId(genreId, start, limit, new Callback());
        apiGateway.simulateTextResponse(200, TestResponses.ROCK_CHANNELS_JSON, null);
        assertNull(genreChannelGateway.failureResponse);
        assertTrue(genreChannelGateway.onCompleteWasCalled);
        assertNotNull(douban.channels);
        assertThat(douban.channels.size(), equalTo(1));
        assertThat(douban.channels.get(0).name, equalTo("摇滚"));
    }

    @Test
    public void shouldHaveCookie() throws Exception {
        genreChannelGateway.fetchChannelsByGenreId(genreId, start, limit, new Callback());
        ApiRequest apiRequest = apiGateway.getLatestRequest();
        TestCase.assertTrue(apiRequest.getHeaders().containsKey("Cookie"));
        assertThat(apiRequest.getHeaders().get("Cookie").toString(), equalTo(""));
    }

    @Test
    public void shouldCallOnFailureWhenParseRespError() throws Exception {
        genreChannelGateway.fetchChannelsByGenreId(genreId, start, limit, new Callback());
        apiGateway.simulateTextResponse(200, TestResponses.NULL_RESP, null);
        assertNotNull(genreChannelGateway.failureResponse);
        assertThat(douban.apiRespErrorCode.getCode(), equalTo("500"));

    }
    @Test
    public void shouldCallOnFailureWhenCallerError() throws Exception {
        genreChannelGateway.fetchChannelsByGenreId(genreId, start, limit, badCallback);
        apiGateway.simulateTextResponse(200, TestResponses.ROCK_CHANNELS_JSON, null);
        assertNotNull(genreChannelGateway.failureResponse);
        assertThat(douban.apiRespErrorCode.getCode(), equalTo("-1"));

    }
}