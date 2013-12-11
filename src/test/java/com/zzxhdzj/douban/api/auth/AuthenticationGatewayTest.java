package com.zzxhdzj.douban.api.auth;

import com.google.common.net.HttpHeaders;
import com.zzxhdzj.douban.api.BaseGatewayTestCase;
import com.zzxhdzj.douban.api.mock.TestResponses;
import com.zzxhdzj.douban.modules.LoginParams;
import com.zzxhdzj.douban.modules.LoginParamsBuilder;
import com.zzxhdzj.http.Callback;
import com.zzxhdzj.http.util.HiUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: yangning.roy
 * Date: 10/29/13
 * Time: 12:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class AuthenticationGatewayTest extends BaseGatewayTestCase {
    private LoginParams loginParams;
    private AuthenticationGateway authenticationGateway;

    @Before
    public void setUp() {
        super.setUp();
        authenticationGateway = new AuthenticationGateway(douban, apiGateway);
        loginParams = LoginParamsBuilder.aLoginParams()
                .withRemember("on")
                .withSource("radio")
                .withCaptcha("cheese")
                .withLoginMail("test@gmail.com")
                .withPassword("password")
                .build();

    }

    //test#01
    @Test
    public void shouldMakeARemoteCallWhenSigningInWithCaptchaCode() {
        authenticationGateway.signIn(loginParams, new Callback());
        String urlString = apiGateway.getLatestRequest().getUrlString();
        assertThat(urlString, equalTo("http://douban.fm/j/login"));
    }

    //test#02
    @Test
    public void shouldSendLoginParams() throws Exception {
        authenticationGateway.signIn(loginParams, new Callback());
        AuthenticationRequest authenticationRequest = (AuthenticationRequest) apiGateway.getLatestRequest();
        assertThat(authenticationRequest, equalTo(new AuthenticationRequest(loginParams)));
        HttpEntity postEntity = authenticationRequest.getPostEntity();
        assertThat(postEntity.getContentType().getValue(), equalTo("application/x-www-form-urlencoded; charset=UTF-8"));
        String content = HiUtil.dump(postEntity);
        assertThat(content, equalTo("remember=on&captcha_id=&captcha_solution=cheese&source=radio&alias=test%40gmail.com&form_password=password"));
    }

    //test#03
    @Test
    public void shouldReturnTrueSignedIn() throws Exception {
        assertThat(douban.isAuthenticated(), equalTo(false));
        authenticationGateway.signIn(loginParams, new Callback());
        Header[] header = new Header[1];
        header[0] = new BasicHeader(HttpHeaders.SET_COOKIE, "ue=\"xxxx@gmail.com\"; domain=.douban.com; expires=Mon, 24-Nov-2014 16:29:27 GMT,fmNlogin=\"y\"; path=/; domain=.douban.fm; expires=Tue, 24-Dec-2013 16:29:27 GMT,bid=\"jAT3l2qRKfc\"; path=/; domain=.douban.com; expires=Mon, 24-Nov-2014 16:29:27 GMT,dbcl2=\"69077079:YhfWsJoFZ00\"; path=/; domain=.douban.fm; expires=Tue, 24-Dec-2013 16:29:27 GMT; httponly,ck=\"10se\"; path=/; domain=.douban.fm");
        apiGateway.simulateTextResponse(200, TestResponses.AUTH_SUCCESS, header);
        assertThat(douban.isAuthenticated(), equalTo(true));
    }

    @Test
    public void shouldReturnFalseWhenSignedInWithCaptchaCodeError() throws Exception {
        assertThat(douban.isAuthenticated(), equalTo(false));
        authenticationGateway.signIn(loginParams, new Callback());
        Header[] header = new Header[0];
        apiGateway.simulateTextResponse(200, TestResponses.AUTH_ERROR, header);
        assertThat(douban.isAuthenticated(), equalTo(false));
        assertThat(douban.apiRespErrorCode.getCode(), equalTo("1011"));
        assertThat(douban.apiRespErrorCode.getMsg(), equalTo("验证码不正确"));
    }

    @Test
    public void shouldCallOnFailureWhenParseRespError() throws Exception {
        authenticationGateway.signIn(loginParams, new Callback());
        apiGateway.simulateTextResponse(200, TestResponses.NULL_RESP, null);
        assertNotNull(authenticationGateway.failureResponse);
        assertThat(douban.apiRespErrorCode.getCode(),equalTo("500"));

    }

    @Test
    public void shouldCallOnFailureWhenCallerError() throws Exception {
        authenticationGateway.signIn(loginParams, badCallback);
        apiGateway.simulateTextResponse(200, TestResponses.AUTH_SUCCESS, null);
        assertNotNull(authenticationGateway.failureResponse);
        assertThat(douban.apiRespErrorCode.getCode(),equalTo("-1"));
    }
}