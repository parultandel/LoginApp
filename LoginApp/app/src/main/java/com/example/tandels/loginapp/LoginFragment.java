package com.example.tandels.loginapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.tandels.loginapp.model.SignInRequest;
import com.example.tandels.loginapp.model.SignInResponse;
import com.example.tandels.loginapp.ui.LoginBaseFragment;
import com.example.tandels.loginapp.util.LoginAppConstants;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

/**
 * LoginFragment class
 */
public class LoginFragment extends LoginBaseFragment {
    //TextInput Layout
    @BindView(R.id.text_input_email)
    TextInputLayout text_input_email;
    @BindView(R.id.text_input_password)
    TextInputLayout text_input_password;

    // EditText
    @BindView(R.id.edt_email)
    EditText edt_email;
    @BindView(R.id.edt_password)
    EditText edt_password;

    //Button
    @BindView(R.id.btnLogin)
    Button btnLogin;

    private CompositeSubscription loginValidationSubscriptions;//group of subscriptions
    private rx.Observable<CharSequence> mEmailTextObservable, mPasswordTextObservable;


    /**
     * getLayoutResource method
     * @return
     */
    @Override
    protected int getLayoutResource() {
        return R.layout.loginfragment;
    }


    /**
     * onView created
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginValidationSubscriptions = new CompositeSubscription();
        initEditTextObservable();//init observable
        initSubscriber();//init validation subscribers
    }


    /**
     * init edittext observable
     */
    private void initEditTextObservable() {
        mEmailTextObservable = RxTextView.textChanges(edt_email);
        mPasswordTextObservable = RxTextView.textChanges(edt_password);
    }


    /**
     * initialize application subscribers
     */
    private void initSubscriber() {
        //initialize validation subscribers
        initEmailValidationSubscriber();
        initPasswordValidationSubscriber();
        initLoginValidationSubsriber();

    }

    /**
     * Email Validation
     */
    private void initEmailValidationSubscriber() {
        //=========== For Email Validation
        Subscription mEmailSubscription = mEmailTextObservable.doOnNext(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                emailEditTextError(1); // disable email error
            }
        })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence) {
                        return !TextUtils.isEmpty(charSequence); // check if not null
                    }
                }).observeOn(AndroidSchedulers.mainThread()) // Main UI Thread
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {
                        Log.e("initEmailvalidate", "Method initEmailValidationSubscriber completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        // Error
                        Log.e("mEmailSubscription", e.getMessage());
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        // Check every user input for valid email address
                        if (!isUserInputValid(charSequence.toString(), 1)) {
                            emailEditTextError(2); // show error for invalid email
                        } else {
                            emailEditTextError(1); // hide error on valid email
                        }
                    }
                });
        loginValidationSubscriptions.add(mEmailSubscription); // Add email subscriber in composite subscription


    }

    /**
     * paswword validation
     */
    private void initPasswordValidationSubscriber() {
        //=========== For Password Validation
        Subscription mPasswordSubscription = mPasswordTextObservable
                .doOnNext(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        passwordEditTextError(1);
                    }
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence) {
                        return !TextUtils.isEmpty(charSequence);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {
                        Log.e("initPasswdValidate", "Method initPasswordValidationSubscriber completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("mPasswordSubscription", e.getMessage());
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        if (!isUserInputValid(charSequence.toString(), 2)) {
                            passwordEditTextError(2);
                        } else {
                            passwordEditTextError(1);
                        }
                    }
                });

        loginValidationSubscriptions.add(mPasswordSubscription);
    }

    /**
     * Login validation
     */
    private void initLoginValidationSubsriber() {
        // Check Both user input ( email and password)
        Subscription allFieldsSubscription = rx.Observable.combineLatest(mEmailTextObservable, mPasswordTextObservable, new Func2<CharSequence, CharSequence, Boolean>() {
            @Override
            public Boolean call(CharSequence mEmail, CharSequence mPassword) {
                return isUserInputValid(mEmail.toString(), 1) && isUserInputValid(mPassword.toString(), 2);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Log.e("initLoginValidate", "Method initLoginValidationSubsriber completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("allFieldsSubscription", e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            signInButtonState(1); // enable login button
                        } else {
                            signInButtonState(2); // disable login button
                        }
                    }
                });
        loginValidationSubscriptions.add(allFieldsSubscription);
    }

    /**
     * Enable and disable Email error as per case
     *
     * @param whichCase: 1 -> for hide , 2 -> for show
     */
    private void emailEditTextError(int whichCase) {
        switch (whichCase) {
            case 1: // for hide error
                if (text_input_email.getChildCount() == 2) {
                    text_input_email.getChildAt(1).setVisibility(View.GONE);
                }
                text_input_email.setError(null);
                break;
            case 2: // for show error
                if (text_input_email.getChildCount() == 2) {
                    text_input_email.getChildAt(1).setVisibility(View.VISIBLE);
                }
                text_input_email.setError(getString(R.string.str_enter_valid_email));
                break;
        }
    }

    /**
     * Enable and disable Email error as per case
     *
     * @param whichCase: 1 -> for hide , 2 -> for show
     */
    private void passwordEditTextError(int whichCase) {
        switch (whichCase) {
            case 1: // for hide error
                if (text_input_password.getChildCount() == 2) {
                    text_input_password.getChildAt(1).setVisibility(View.GONE);
                }
                text_input_password.setError(null);
                break;
            case 2: // for show error
                if (text_input_password.getChildCount() == 2) {
                    text_input_password.getChildAt(1).setVisibility(View.VISIBLE);
                }
                text_input_password.setError(getString(R.string.str_enter_password));
                break;
        }
    }

    /**
     * Enable and disable login button as per case
     *
     * @param whichCase : 1 -> enable , 2 ->  disable
     */
    private void signInButtonState(int whichCase) {
        switch (whichCase) {
            case 1: // enable button
                btnLogin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                btnLogin.setEnabled(true);
                btnLogin.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                break;
            case 2: // disable button
                btnLogin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_disable));
                btnLogin.setEnabled(false);
                btnLogin.setTextColor(ContextCompat.getColor(getContext(), R.color.color_disable_text));
                break;
        }

    }

    /**
     * Validate user details for email and password
     */

    private boolean isUserInputValid(String userInput, int whichCase) {
        switch (whichCase) {
            case 1: // check email input
                return !TextUtils.isEmpty(userInput) && Patterns.EMAIL_ADDRESS.matcher(userInput).matches();
            case 2: // check password input
                return userInput.length() >= LoginAppConstants.EXPECTED_PASSWD_LENGTH;
        }
        return false;
    }

    /**
     * Validate login
     *
     * @param signInRequest
     * @return SignInResponse
     */
    public SignInResponse validateLogin(SignInRequest signInRequest) {
        if (signInRequest != null
                && LoginAppConstants.EXPECTED_EMAIL.equalsIgnoreCase(signInRequest.getEmail())
                && LoginAppConstants.EXPECTED_PASSWORD.equals(signInRequest.getPassword())) {
            return new SignInResponse(LoginAppConstants.SUCCESS_STATUS);
        }
        return new SignInResponse(LoginAppConstants.FAILURE_STATUS);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        loginValidationSubscriptions.unsubscribe();

    }

    @OnClick(R.id.btnLogin)
    public void setOnClick() {
        SignInRequest signInRequest = new SignInRequest(edt_email.getText().toString(), edt_password.getText().toString());
        SignInResponse signResponse = validateLogin(signInRequest);
        if (LoginAppConstants.SUCCESS_STATUS == signResponse.getStatusCode()) {
            showToast(getString(R.string.str_login_success));
        } else {
            showToast(getString(R.string.str_login_failed));
        }
    }
}
