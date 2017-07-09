package me.aflak.libraries;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Created by Omar on 02/07/2017.
 */

public class FingerprintDialog {
    private Context context;
    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;
    private FingerprintCallback fingerprintCallback;
    private FingerprintManager.CryptoObject cryptoObject;
    private KeyStoreHelper keyStoreHelper;

    private LayoutInflater layoutInflater;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private View view;

    private String title, message;

    private boolean canceledOnTouchOutside;
    private int enterAnimation, exitAnimation, successColor, errorColor;

    public final static int ENTER_FROM_BOTTOM=0, ENTER_FROM_TOP=1, ENTER_FROM_LEFT=2, ENTER_FROM_RIGHT=3;
    public final static int EXIT_TO_BOTTOM=0, EXIT_TO_TOP=1, EXIT_TO_LEFT=2, EXIT_TO_RIGHT=3;
    public final static int NO_ANIMATION=4;

    private final static String TAG = "FingerprintDialog";
    private final String keyName = "FingerprintDialogLibraryKey";

    public FingerprintDialog(Context context, FingerprintManager fingerprintManager){
        this.context = context;
        this.fingerprintManager = fingerprintManager;
        init(context);
    }

    public FingerprintDialog(Context context){
        this.context = context;
        this.fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        init(context);
    }

    private void init(Context context){
        this.keyStoreHelper = new KeyStoreHelper(keyName);
        this.layoutInflater = LayoutInflater.from(context);
        this.builder = new AlertDialog.Builder(context);
        this.successColor = R.color.auth_success;
        this.errorColor = R.color.auth_failed;
        this.canceledOnTouchOutside = false;
        this.enterAnimation = ENTER_FROM_BOTTOM;
        this.exitAnimation = EXIT_TO_BOTTOM;
        this.cryptoObject = null;
    }

    public boolean isHardwareDetected(){
        return fingerprintManager.isHardwareDetected();
    }

    public boolean hasEnrolledFingerprints(){
        return fingerprintManager.hasEnrolledFingerprints();
    }

    public void setAnimation(int enterAnimation, int exitAnimation) {
        this.enterAnimation = enterAnimation;
        this.exitAnimation = exitAnimation;

    }

    public void setSuccessColor(int successColor){
        this.successColor = successColor;
    }

    public void setErrorColor(int errorColor){
        this.errorColor = errorColor;
    }

    public void setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.canceledOnTouchOutside = canceledOnTouchOutside;
    }

    public void generateNewKey(){
        if(keyStoreHelper!=null){
            keyStoreHelper.generateNewKey();
        }
    }

    public void showSecure(int resTitle, int resMessage, FingerprintSecureCallback fingerprintSecureCallback){
        showSecure(context.getResources().getString(resTitle), context.getResources().getString(resMessage), fingerprintSecureCallback);
    }

    public void showSecure(String title, String message, final FingerprintSecureCallback fingerprintSecureCallback){
        this.title = title;
        this.message = message;
        this.fingerprintCallback = fingerprintSecureCallback;

        keyStoreHelper.getCryptoObject(new KeyStoreHelperCallback() {
            @Override
            public void onNewFingerprintEnrolled() {
                if(fingerprintSecureCallback!=null){
                    fingerprintSecureCallback.onNewFingerprintEnrolled();
                }
            }

            @Override
            public void onCryptoObjectRetrieved(FingerprintManager.CryptoObject cryptoObject) {
                FingerprintDialog.this.cryptoObject = cryptoObject;
                show();
            }
        });
    }

    public void show(int resTitle, int resMessage, FingerprintCallback fingerprintCallback){
        show(context.getResources().getString(resTitle), context.getResources().getString(resMessage), fingerprintCallback);
    }

    public void show(String title, String message, FingerprintCallback fingerprintCallback){
        this.cryptoObject = null;
        this.title = title;
        this.message = message;
        this.fingerprintCallback = fingerprintCallback;

        show();
    }

    private void show(){
        view = layoutInflater.inflate(R.layout.dialog, null);
        ((TextView) view.findViewById(R.id.dialog_title)).setText(title);
        ((TextView) view.findViewById(R.id.dialog_message)).setText(message);
        builder.setPositiveButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancellationSignal.cancel();
                if(fingerprintCallback!=null) {
                    fingerprintCallback.onCancelled();
                }
            }
        });
        builder.setView(view);
        dialog = builder.create();
        if(dialog.getWindow() != null && (enterAnimation!=NO_ANIMATION || exitAnimation!=NO_ANIMATION)) {
            int style = getStyle();
            if(style==-1){
                Log.e(TAG, "The animation selected is not available");
            }
            else {
                dialog.getWindow().getAttributes().windowAnimations = style;
            }
        }
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.show();

        auth();
    }

    private int getStyle(){
        switch (enterAnimation){
            case ENTER_FROM_BOTTOM:
                switch (exitAnimation){
                    case EXIT_TO_BOTTOM:
                        return R.style.BottomBottomAnimation;
                    case EXIT_TO_TOP:
                        return R.style.BottomTopAnimation;
                    case NO_ANIMATION:
                        return R.style.EnterFromBottomAnimation;
                }
                break;
            case ENTER_FROM_TOP:
                switch (exitAnimation){
                    case EXIT_TO_BOTTOM:
                        return R.style.TopBottomAnimation;
                    case EXIT_TO_TOP:
                        return R.style.TopTopAnimation;
                    case NO_ANIMATION:
                        return R.style.EnterFromTopAnimation;
                }
                break;
            case ENTER_FROM_LEFT:
                switch (exitAnimation){
                    case EXIT_TO_LEFT:
                        return R.style.LeftLeftAnimation;
                    case EXIT_TO_RIGHT:
                        return R.style.LeftRightAnimation;
                    case NO_ANIMATION:
                        return R.style.EnterFromLeftAnimation;
                }
                break;
            case ENTER_FROM_RIGHT:
                switch (exitAnimation){
                    case EXIT_TO_LEFT:
                        return R.style.RightLeftAnimation;
                    case EXIT_TO_RIGHT:
                        return R.style.RightRightAnimation;
                    case NO_ANIMATION:
                        return R.style.EnterFromRightAnimation;
                }
                break;
            case NO_ANIMATION:
                switch (exitAnimation){
                    case EXIT_TO_BOTTOM:
                        return R.style.ExitToBottomAnimation;
                    case EXIT_TO_TOP:
                        return R.style.ExitToTopAnimation;
                    case EXIT_TO_LEFT:
                        return R.style.ExitToLeftAnimation;
                    case EXIT_TO_RIGHT:
                        return R.style.ExitToRightAnimation;
                }
                break;
        }
        return -1;
    }

    private void auth(){
        cancellationSignal = new CancellationSignal();
        if(fingerprintManager.isHardwareDetected()) {
            if (fingerprintManager.hasEnrolledFingerprints()) {
                fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        setStatus(errString.toString(), errorColor, R.drawable.fingerprint_error, null);
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        super.onAuthenticationHelp(helpCode, helpString);
                        setStatus(helpString.toString(), errorColor, R.drawable.fingerprint_error, null);
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        setStatus(R.string.state_success, successColor, R.drawable.fingerprint_success, new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                dialog.cancel();
                                if (fingerprintCallback != null) {
                                    fingerprintCallback.onAuthenticated();
                                }
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        setStatus(R.string.state_failure, errorColor, R.drawable.fingerprint_error, null);
                    }
                }, null);
            }
            else{
                Log.e(TAG, "No fingerprint enrolled");
            }
        }
        else{
            Log.e(TAG, "No fingerprint scanner detected");
        }
    }


    private void setStatus(int textId, int color, int drawable, YoYo.AnimatorCallback callback){
        setStatus(context.getResources().getString(textId), color, drawable, callback);
    }

    private void setStatus(final String text, final int color, final int drawable, final YoYo.AnimatorCallback callback){
        final RelativeLayout layout = view.findViewById(R.id.dialog_layout_icon);
        final View background = view.findViewById(R.id.dialog_icon_background);
        final ImageView foreground = view.findViewById(R.id.dialog_icon_foreground);
        final TextView status = view.findViewById(R.id.dialog_status);

        YoYo.with(Techniques.FlipOutY)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        status.setText(text);
                        status.setTextColor(ContextCompat.getColor(context, color));
                        background.setBackgroundTintList(ColorStateList.valueOf(context.getColor(color)));
                        foreground.setImageResource(drawable);

                        if(callback!=null) {
                            YoYo.with(Techniques.FlipInY)
                                    .onEnd(callback)
                                    .duration(350)
                                    .playOn(layout);
                        }
                        else{
                            YoYo.with(Techniques.FlipInY)
                                    .duration(350)
                                    .playOn(layout);
                        }
                    }
                })
                .duration(350)
                .playOn(layout);
    }
}
