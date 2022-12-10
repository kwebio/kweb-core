package kweb.https

import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.extensions.HashAlgorithm
import io.ktor.network.tls.extensions.SignatureAlgorithm
import io.ktor.server.engine.ConnectorType
import io.ktor.server.engine.EngineSSLConnectorConfig
import java.io.File
import java.security.KeyStore
import kotlin.text.toCharArray

@Deprecated("Obsolete, this class will be deleted in a future release")
val defaultKeyStore by lazy {
    buildKeyStore {
        certificate("test") {
            hash = HashAlgorithm.SHA1
            sign = SignatureAlgorithm.RSA
            password = "changeit"
        }
    }
}

@Deprecated("Obsolete, this class will be deleted in a future release")
data class SSLConfig constructor(
    override val port: Int = 9091,
    override val host: String = "0.0.0.0",
    override val keyStore: KeyStore = defaultKeyStore,
    override val keyStorePath: File? = null,
    override val keyAlias: String = "mykey",
    override val keyStorePassword: () -> CharArray = { "changeit".toCharArray() },
    override val privateKeyPassword: () -> CharArray = { "changeit".toCharArray() },
    override val trustStore: KeyStore? = null,
    override val trustStorePath: File? = null,
    override val enabledProtocols: List<String>? = null,
) : EngineSSLConnectorConfig {
    override val type: ConnectorType = ConnectorType.HTTPS
}