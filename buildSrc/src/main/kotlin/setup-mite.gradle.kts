import downlaod.DownloadResult
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toOkioPath

private fun download(url: String, directory: Path, filename: String): DownloadResult {
    val client = OkHttpClient()
    val file = directory.resolve(filename)
    val temp = directory.resolve("$filename.temp")

    try {
        FileSystem.SYSTEM.createDirectories(directory)
    } catch (e: IOException) {
        return DownloadResult.Failure("Cannot create directory: ${e.message}")
    }

    val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Gradle Plugin")
        .build()

    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                FileSystem.SYSTEM.delete(temp, true)
                return DownloadResult.Failure("Response code ${response.code}")
            }

            val file2j = file.toFile()

            if (file2j.exists()) {
                val length = response.body?.contentLength() ?: -1
                if (length == file2j.length()) {
                    return DownloadResult.Failure("File is already exist!")
                }
            }

            val bodySource = response.body?.source()

            if (bodySource == null) {
                FileSystem.SYSTEM.delete(temp, true)
                return DownloadResult.Failure("Body source is null")
            }

            FileSystem.SYSTEM.write(temp) sink@{
                this@sink.writeAll(bodySource)
            }

            FileSystem.SYSTEM.atomicMove(temp, file)
        }
        return DownloadResult.Success
    } catch (e: IOException) {
        FileSystem.SYSTEM.delete(temp, true)
        return DownloadResult.Failure(e.message ?: "Unknown error")
    }
}

private val DOWNLOAD_URL =
    "https://maven.limingzxc.top/repository/maven-releases/com/mojang/minecraft/1.6.4-MITE/minecraft-1.6.4-MITE.jar"

configurations.whenObjectAdded configuration@{
    if (this@configuration.name == "minecraft") {
        this@configuration.dependencies.whenObjectAdded dependency@{
            val version = this@dependency.version ?: throw GradleException("No version")

            if (version.isEmpty() || version.isBlank()) {
                throw GradleException("Version is empty or blank")
            }

            val gradle = project.gradle.gradleUserHomeDir.toOkioPath()
            val directory = gradle
                .resolve("caches")
                .resolve("fml-loom")
                .resolve(version)

            val result = download(
                url = DOWNLOAD_URL,
                directory = directory,
                filename = "$version.jar"
            )

            when (result) {
                is DownloadResult.Success -> logger.lifecycle("Download mite successful: $DOWNLOAD_URL")
                is DownloadResult.Failure -> logger.lifecycle("Download mite failed: ${result.message}")
            }
        }
    }
}

