package com.example.hybridcryptographywithfirebase.viewmodels

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hybridcryptographywithfirebase.models.EncryptedMessage
import com.example.hybridcryptographywithfirebase.utils.Constants
import com.example.hybridcryptographywithfirebase.utils.Constants.AES_CRYPTO_ALGORITHM
import com.example.hybridcryptographywithfirebase.utils.Constants.AES_KEY_SIZE
import com.example.hybridcryptographywithfirebase.utils.Constants.AES_TRANSFORMATION
import com.example.hybridcryptographywithfirebase.utils.Constants.CRYPTOGRAPHY
import com.example.hybridcryptographywithfirebase.utils.Constants.ENCRYPTED_MESSAGE
import com.example.hybridcryptographywithfirebase.utils.Constants.IV_BUFFER
import com.example.hybridcryptographywithfirebase.utils.Constants.RSA_CRYPTO_ALGORITHM
import com.example.hybridcryptographywithfirebase.utils.Constants.RSA_KEY_SIZE
import com.example.hybridcryptographywithfirebase.utils.Constants.RSA_TRANSFORMATION
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel: ViewModel() {

    private val _privateKeyOfServer: MutableLiveData<String> = MutableLiveData()
    val privateKeyOfServer: LiveData<String> = _privateKeyOfServer

    private val _publicKeyOfServer: MutableLiveData<String> = MutableLiveData()
    val publicKeyOfServer: LiveData<String> = _publicKeyOfServer

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    val databaseReference: DatabaseReference = firebaseDatabase.getReference(CRYPTOGRAPHY)

    private val _encryptedMessage: MutableLiveData<EncryptedMessage> = MutableLiveData()
    val encryptedMessage: LiveData<EncryptedMessage> = _encryptedMessage

    /**
     * This method fetches user name available on Firebase node
     */
    suspend fun getCurrentValueFromFirebaseNode(): Long = suspendCoroutine { continuation ->
        databaseReference.child(Constants.VALUE).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                when(val currentValue = task.result?.value.also { Log.d(TAG, "$it") }) {
                    is Long -> continuation.resume(currentValue)
                    else -> Log.d(TAG, "Current value is null")
                }
            }
        }
    }

    fun generateServerKeyPair() {
        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(RSA_CRYPTO_ALGORITHM)
        keyPairGenerator.initialize(RSA_KEY_SIZE)
        val keyPair: KeyPair = keyPairGenerator.generateKeyPair()
        Base64.encodeToString(keyPair.private.encoded, Base64.DEFAULT).also { privateKey ->
            _privateKeyOfServer.postValue(privateKey)
        }

        Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT).also { publicKey ->
            _publicKeyOfServer.postValue(publicKey)
        }
    }

    fun encryptMessage(receiversPublicKey: String, messageToBeEncrypted: String) = try {
        /**
         * Generate secret key using AES
         */
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance(AES_CRYPTO_ALGORITHM)
        keyGenerator.init(AES_KEY_SIZE)
        val secretKey: SecretKey = keyGenerator.generateKey()

        /**
         * Encrypt message using secret key
         */
        val raw: ByteArray = secretKey.encoded
        val secretKeySpec = SecretKeySpec(raw, AES_CRYPTO_ALGORITHM)
        val cipher: Cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(byteArrayOf(16)))
        val cipherText: String = Base64.encodeToString(cipher.doFinal(messageToBeEncrypted.toByteArray(Charset.forName("UTF-8"))), Base64.DEFAULT)

        /**
         * Get public key
         */
        val publicSpec = X509EncodedKeySpec(Base64.decode(receiversPublicKey, Base64.DEFAULT))
        val keyFactory: KeyFactory = KeyFactory.getInstance(RSA_CRYPTO_ALGORITHM)
        val publicKey: PublicKey = keyFactory.generatePublic(publicSpec)

        /**
         * Encrypt secret key using receiver's public key
         */

        val cipher2: Cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher2.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedSecretKey = Base64.encodeToString(cipher2.doFinal(secretKey.encoded), Base64.DEFAULT)

        _encryptedMessage.postValue(EncryptedMessage(encryptedSecretKey, cipherText))

    } catch (e: Exception) {
        e.printStackTrace()
    }

    fun postEncryptedMessage(encryptedMessage: EncryptedMessage) {
        databaseReference.child(ENCRYPTED_MESSAGE).setValue(encryptedMessage)
    }

}