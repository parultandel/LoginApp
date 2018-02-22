package com.example.tandels.loginapp.ui;

import com.example.tandels.loginapp.LoginFragment;
import com.example.tandels.loginapp.model.SignInRequest;
import com.example.tandels.loginapp.model.SignInResponse;
import com.example.tandels.loginapp.util.LoginAppConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)

public class LoginFragmentTest {


    private SignInRequest signInRequestMock;
    private SignInResponse signInResponseMock;


    @Before
    public void setUp() throws Exception {
    }
    @Test
    public void testValidateLoginFailureStatus()
    {
        LoginFragment loginFragment=new LoginFragment();
        signInRequestMock=new SignInRequest("username","password");
        signInResponseMock = loginFragment.validateLogin(signInRequestMock);
        assertNotNull(signInResponseMock);
        assertEquals(LoginAppConstants.FAILURE_STATUS,signInResponseMock.getStatusCode());
        signInRequestMock=new SignInRequest(null,null);
        signInResponseMock = loginFragment.validateLogin(signInRequestMock);
        assertNotNull(signInResponseMock);
        assertEquals(LoginAppConstants.FAILURE_STATUS,signInResponseMock.getStatusCode());

    }

    @Test
    public void testValidateLoginSuccessStatus()
    {
        LoginFragment loginFragment=new LoginFragment();
        signInRequestMock=new SignInRequest(LoginAppConstants.EXPECTED_EMAIL,LoginAppConstants.EXPECTED_PASSWORD);
        signInResponseMock = loginFragment.validateLogin(signInRequestMock);
        assertNotNull(signInResponseMock);
        assertEquals(LoginAppConstants.SUCCESS_STATUS,signInResponseMock.getStatusCode());
    }
}
