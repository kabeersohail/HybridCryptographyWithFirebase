package com.example.hybridcryptographywithfirebase.viewmodels

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hybridcryptographywithfirebase.models.EncryptedMessage
import com.example.hybridcryptographywithfirebase.utils.Constants
import com.example.hybridcryptographywithfirebase.utils.Constants.CRYPTOGRAPHY
import com.example.hybridcryptographywithfirebase.utils.Constants.DECRYPTED_MESSAGE
import com.example.hybridcryptographywithfirebase.utils.Constants.ENCRYPTED_MESSAGE
import com.example.hybridcryptographywithfirebase.utils.Constants.MY_DEVICE
import com.example.hybridcryptographywithfirebase.utils.Constants.RSA_CRYPTO_ALGORITHM
import com.example.hybridcryptographywithfirebase.utils.Constants.RSA_KEY_SIZE
import com.example.hybridcryptographywithfirebase.utils.Constants.SERVER_PUBLIC_KEY
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.nio.charset.Charset
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.*
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
    private val databaseReference: DatabaseReference = firebaseDatabase.getReference(CRYPTOGRAPHY)

    private val _encryptedMessage: MutableLiveData<EncryptedMessage> = MutableLiveData()
    val encryptedMessage: LiveData<EncryptedMessage> = _encryptedMessage

    private val _decryptedMessage: MutableLiveData<String> = MutableLiveData()
    val decryptedMessage: LiveData<String> = _decryptedMessage

    /**
     * This method fetches user name available on Firebase node
     */
    suspend fun getEncryptedMessageFromFirebase(): EncryptedMessage = suspendCoroutine { continuation ->
        databaseReference.child(MY_DEVICE).child(ENCRYPTED_MESSAGE).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                when(val currentValue = task.result?.value.also { Log.d(TAG, "$it") }) {
                    is HashMap<*, *> -> {
                        val map: Map<String, String> = currentValue as? Map<String, String> ?: return@addOnCompleteListener
                        continuation.resume(EncryptedMessage.from(map))
                    }
                    else -> {
                        Log.d(TAG, "Current value is null ${currentValue?.javaClass?.name}")
                    }
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

    fun encryptMessage(receiversPublicKey: String, messageToBeEncrypted: String) {
        try {

            // 1. generate secret key using AES
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(128) // AES is currently available in three key sizes: 128, 192 and 256 bits.The design and strength of all key lengths of the AES algorithm are sufficient to protect classified information up to the SECRET level
            val secretKey = keyGenerator.generateKey()

            // 3. encrypt string using secret key
            val raw = secretKey.encoded
            val skeySpec = SecretKeySpec(raw, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(16)))
            val cipherTextString = Base64.encodeToString(
                cipher.doFinal(messageToBeEncrypted.toByteArray(Charset.forName("UTF-8"))),
                Base64.DEFAULT
            )

            // 4. get public key
            val publicSpec = X509EncodedKeySpec(Base64.decode(receiversPublicKey, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey = keyFactory.generatePublic(publicSpec)

            // 6. encrypt secret key using public key
            val cipher2 = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
            cipher2.init(Cipher.ENCRYPT_MODE, publicKey)
            val encryptedSecretKey =
                Base64.encodeToString(cipher2.doFinal(secretKey.encoded), Base64.DEFAULT)

            _encryptedMessage.postValue(EncryptedMessage(encryptedSecretKey, cipherTextString))

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }
    }

    fun decryptMessage(serverPrivateKey: String, encryptedSecretKeyString: String, encryptedTextString: String) {
        try {

            // 1. Get private key
            val privateSpec = PKCS8EncodedKeySpec(Base64.decode(serverPrivateKey, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            val privateKey = keyFactory.generatePrivate(privateSpec)

            // 2. Decrypt encrypted secret key using private key
            val cipher1 = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
            cipher1.init(Cipher.DECRYPT_MODE, privateKey)
            val secretKeyBytes =
                cipher1.doFinal(Base64.decode(encryptedSecretKeyString, Base64.DEFAULT))
            val secretKey: SecretKey = SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.size, "AES")

            // 3. Decrypt encrypted text using secret key
            val raw = secretKey.encoded
            val skeySpec = SecretKeySpec(raw, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(16)))
            val original = cipher.doFinal(Base64.decode(encryptedTextString, Base64.DEFAULT))
            val text = String(original, Charset.forName("UTF-8"))

            _decryptedMessage.postValue(text)

            // 4. Print the original text sent by client
            println("text\n$text\n\n")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }
    }

    fun postEncryptedMessage(encryptedMessage: EncryptedMessage) {
        databaseReference.child(MY_DEVICE).child(ENCRYPTED_MESSAGE).setValue(encryptedMessage)
    }

    fun postDecryptedMessage(decryptedMessage: String) {
        databaseReference.child(MY_DEVICE).child(DECRYPTED_MESSAGE).setValue(decryptedMessage)
    }

    fun postPublicKey(publicKey: String) {
        databaseReference.child(MY_DEVICE).child(SERVER_PUBLIC_KEY).setValue(publicKey)
    }

    suspend fun getServerPublicKey(): String = suspendCoroutine { continuation ->
        databaseReference.child(MY_DEVICE).child(SERVER_PUBLIC_KEY).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                when(val currentValue = task.result?.value.also { Log.d(TAG, "$it") }) {
                    is String -> continuation.resume(currentValue)
                    else -> Log.d(TAG, "Current value is null")
                }
            }
        }
    }

}