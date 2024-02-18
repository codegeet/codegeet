package io.codegeet.platform.config

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import io.codegeet.platform.config.DockerConfiguration.DockerContainerConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.time.Duration

@Configuration
@EnableConfigurationProperties(DockerContainerConfiguration::class)
class DockerConfiguration(private val environment: Environment) {

    // todo Configure Docker Client similar to https://github.com/bmuschko/gradle-docker-plugin/blob/e5abdc28e483905bacd49ae49738482dbd567a60/src/main/java/com/bmuschko/gradle/docker/internal/services/DockerClientService.java#L75
    @Bean
    fun dockerClient(): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()

        val dockerHttpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(50)
            .connectionTimeout(Duration.ofSeconds(15))
            .responseTimeout(Duration.ofSeconds(30))
            .build()

        val dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient)

        try {
            dockerClient.pingCmd().exec()
        } catch (e: Exception) {
            throw RuntimeException("Docker client initialization failed. Docker host: '${config.dockerHost}'.", e)
        }

        return dockerClient
    }

    @ConfigurationProperties(prefix = "app.docker.container")
    data class DockerContainerConfiguration(
        val memory: Long,
        val cpuPeriod: Long,
        val cpuQuota: Long,
        val timeoutSeconds: Long,
    )
}
