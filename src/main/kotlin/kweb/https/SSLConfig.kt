package kweb.https

import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.extensions.HashAlgorithm
import io.ktor.network.tls.extensions.SignatureAlgorithm
import io.ktor.server.engine.ConnectorType
import io.ktor.server.engine.EngineSSLConnectorConfig
import io.ktor.util.KtorExperimentalAPI
import java.io.File
import java.security.KeyStore

@KtorExperimentalAPI
val defaultKeyStore by lazy {
    buildKeyStore {
        certificate("test") {
            hash = HashAlgorithm.SHA1
            sign = SignatureAlgorithm.RSA
            password = "changeit"
        }
    }
}

data class SSLConfig constructor(
    override val port: Int = 9091,
    override val host: String = "0.0.0.0",
    override val keyStore: KeyStore = defaultKeyStore,
    override val keyStorePath: File? = null,
    override val keyAlias: String = "mykey",
    override val keyStorePassword: () -> CharArray = { "changeit".toCharArray() },
    override val privateKeyPassword: () -> CharArray = { "changeit".toCharArray() }
) : EngineSSLConnectorConfig {
    override val type: ConnectorType = ConnectorType.HTTPS
}