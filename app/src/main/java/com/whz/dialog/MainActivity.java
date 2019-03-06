package com.whz.dialog;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintDialog;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.ConfirmationCallback;
import android.security.ConfirmationDialog;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvResult;
    private KeyguardManager mKeyguardManager;
    private FingerprintManagerCompat mFingerprintManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cv_finger:
                btnFinger();
                break;
            case R.id.cv_digital_code:
                btnDigitalCode();
                break;
            case R.id.cv_confirmation_dialog:
                btnConfirmationDialog();
                break;
            default:
                break;
        }
    }

    /**
     * 点击确认弹窗
     */
    public void btnConfirmationDialog() {
        try {
            new ConfirmationDialog.Builder()
                    .setPromptText("100")
                    .setExtraData(new byte[1024])
                    .build(this)
                    .presentPrompt(getMainExecutor(), new ConfirmationCallback() {
                        @Override
                        public void onConfirmedByUser(byte[] dataThatWasConfirmed) {
                            super.onConfirmedByUser(dataThatWasConfirmed);
                            mTvResult.setText("onConfirmedByUser\ndataThatWasConfirmed:" + dataThatWasConfirmed);
                        }

                        @Override
                        public void onDismissedByUser() {
                            super.onDismissedByUser();
                            mTvResult.setText("onDismissedByUser");
                        }

                        @Override
                        public void onDismissedByApplication() {
                            super.onDismissedByApplication();
                            mTvResult.setText("onDismissedByApplication");
                        }

                        @Override
                        public void onError(Exception e) {
                            super.onError(e);
                            mTvResult.setText("onError:" + e.toString());
                        }
                    });
        } catch (Exception e) {
            mTvResult.setText("Exception:\n" + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 点击数字密码
     */
    public void btnDigitalCode() {
        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "RESULT_OK", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击指纹
     */
    public void btnFinger() {
        //一系列检测
        initCheck();
        //使用android P 默认指纹界面
        initFingerprintDialog();
    }

    /**
     * 使用android P 默认指纹界面
     */
    private void initFingerprintDialog() {
        new FingerprintDialog.Builder()
                .setTitle("请刷入指纹")
                .setSubtitle("需要验证是您本人的指纹")
                .setDescription("请将您的手指放在指纹扫描仪上，让应用程序执行生物认证.")
                .setNegativeButton("取消", getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mTvResult.setText("setNegativeButton i=" + i);
                    }
                })
                .build(this)
                .authenticate(new CancellationSignal(), getMainExecutor(), new FingerprintDialog.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        mTvResult.setText("onAuthenticationError\nerrorCode:" + errorCode + "\nCharSequence:" + errString);
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        super.onAuthenticationHelp(helpCode, helpString);
                        mTvResult.setText("onAuthenticationHelp\nhelpCode:" + helpCode + "\nCharSequence:" + helpString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintDialog.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        mTvResult.setText("onAuthenticationSucceeded\nAuthenticationResult:" + result.getCryptoObject());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        mTvResult.setText("onAuthenticationFailed\n");
                    }
                });
    }

    /**
     * 一系列检测
     */
    private void initCheck() {
        //检测设备是否支持最新指纹特性
        boolean aBoolean = getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
        if (!aBoolean) {
            Toast.makeText(this, "您的设备不支持指纹新特性", Toast.LENGTH_SHORT).show();
            return;
        }
        //检测是否设置密码锁
        if (!mKeyguardManager.isKeyguardSecure()) {
            Toast.makeText(this, "去设置->安全录入数字密码", Toast.LENGTH_SHORT).show();
            return;
        }
        //检测系统内是否有指纹录入
        if (!mFingerprintManagerCompat.hasEnrolledFingerprints()) {
            Toast.makeText(this, "去设置->安全录入指纹", Toast.LENGTH_SHORT).show();
            return;
        }
        //检测设备是否有指纹硬件
        if (!mFingerprintManagerCompat.isHardwareDetected()) {
            Toast.makeText(this, "您的的设备不支持指纹", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    /**
     * 初始化事件
     */
    private void initEvent() {
        mFingerprintManagerCompat = FingerprintManagerCompat.from(this);
        mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
    }

    /**
     * 初始化View
     */
    private void initView() {
        mTvResult = findViewById(R.id.tv_result);

        findViewById(R.id.cv_finger).setOnClickListener(this);
        findViewById(R.id.cv_digital_code).setOnClickListener(this);
        findViewById(R.id.cv_confirmation_dialog).setOnClickListener(this);
    }
}
