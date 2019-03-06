package com.whz.dialog.utils;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.spec.ECGenParameterSpec;

/**
 * Created by kevin on 2018/6/12
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class KeyStoreUtil {

    private final String aTag = KeyStoreUtil.class.getSimpleName();

    private final String KEYSTORE_ALIAS = "MY_KEY";
    private final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private final String SIGNATURE_SHA256withRSA = "SHA256withECDSA";

    public KeyStoreUtil() {
    }

    public static KeyStoreUtil newInstance() {
        return new KeyStoreUtil();
    }

    /**
     * 生成公私钥
     */
    public boolean crateKey() {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_SIGN)
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                    .setAttestationChallenge(genChallenge())
                    .setUserAuthenticationRequired(true)
                    .build();

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE);
            keyPairGenerator.initialize(spec);
            keyPairGenerator.generateKeyPair();
            if (getKeyStoreEntry() != null) {
                return true;
            }
        } catch (Exception e) {
            Log.e(aTag, e.toString());
        }
        return false;
    }

    /**
     * 对数据签名
     *
     * @param inputStr 待签名数据
     * @return 签名后的数据
     */
    public byte[] signData(String inputStr) {
        try {
            //待签名数据
            byte[] data = inputStr.getBytes();
            //获取私钥
            PrivateKey privateKey = getPrivateKey();
            //对传入对数据进行签名
            Signature signature = Signature.getInstance(SIGNATURE_SHA256withRSA);
            signature.initSign(privateKey);
            signature.update(data);
            byte[] result = signature.sign();
            return result;
        } catch (Exception e) {
            Log.e(aTag, "signData：" + e.getMessage());
        }
        return null;
    }

    /**
     * 对数据验签
     *
     * @param inputStr 待签名数据
     * @param signStr  签名后数据
     * @return 验签结果
     */
    public boolean verifyData(String inputStr, byte[] signStr) {
        try {
            //待验签数据
            byte[] data = inputStr.getBytes();
            if (signStr == null) {
                return false;
            }
            //对传入对数据进行签名
            Signature signature = Signature.getInstance(SIGNATURE_SHA256withRSA);
            Certificate certificate = getCertificate();
            signature.initVerify(certificate);
            signature.update(data);
            boolean result = signature.verify(signStr);
            return result;
        } catch (Exception e) {
            Log.e(aTag, "verifyData：" + e.getMessage());
        }
        return false;
    }


    /**
     * 取出证书
     */
    private Certificate getCertificate() {
        return getKeyStoreEntry().getCertificate();
    }


    /**
     * 取出私钥
     */
    private PrivateKey getPrivateKey() {
        return getKeyStoreEntry().getPrivateKey();
    }

    /**
     * 取出KeyStoreEntry
     */
    private KeyStore.PrivateKeyEntry getKeyStoreEntry() {
        try {
            //取出别名数据
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            KeyStore.Entry keyStoreEntry = keyStore.getEntry(KEYSTORE_ALIAS, null);
            //校验存储别名数据
            if (keyStoreEntry == null) {
                return null;
            }
            if (!(keyStoreEntry instanceof KeyStore.PrivateKeyEntry)) {
                return null;
            }
            KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keyStoreEntry;
            return keyEntry;
        } catch (Exception e) {
            Log.e(aTag, "getKeyStoreEntry：" + e.getMessage());
        }
        return null;
    }

    /**
     * 挑战值
     */
    private byte[] genChallenge() {
        SecureRandom random = new SecureRandom();
        byte[] challenge = new byte[32];
        random.nextBytes(challenge);
        return challenge;
    }
}
