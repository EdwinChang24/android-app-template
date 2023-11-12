import java.io.File
import java.time.LocalDate

object Defaults {
    const val PACKAGE_NAME = "com.example.myapplication"
    const val APP_NAME = "My Application"
    const val APP_NAME_AS_CLASS_NAME = "MyApplication"
    const val USERNAME = "ExampleName"
    const val YEAR = "ExampleYear"
}

val templateDir = File("./template")

fun replaceName(packageName: String, appName: String, username: String) {
    val appNameAsClassName =
        appName.split(' ').joinToString(separator = "") { it.replaceFirstChar(Char::uppercaseChar) }
    templateDir.walk().forEach { file ->
        if (!file.isDirectory) {
            file.writeText(
                file.readText().replace(Defaults.PACKAGE_NAME, packageName).replace(Defaults.APP_NAME, appName)
                    .replace(Defaults.APP_NAME_AS_CLASS_NAME, appNameAsClassName).replace(Defaults.USERNAME, username)
                    .replace(Defaults.YEAR, LocalDate.now().year.toString())
            )
        }
    }
    for (folder in listOf("androidTest", "main", "test")) {
        File(
            templateDir, "app/src/$folder/kotlin/${Defaults.PACKAGE_NAME.replace('.', File.separatorChar)}"
        ).renameTo(File(
            templateDir, "app/src/$folder/kotlin/${packageName.replace('.', File.separatorChar)}"
        ).also { it.mkdirs() })
    }
}

fun move() {
    File(".github").deleteRecursively()
    File("scripts/initialize.main.kts").deleteOnExit()
    File("uninitialized.md").delete()
    templateDir.list()?.forEach { file ->
        File(templateDir, file).copyRecursively(File(file), overwrite = true)
    }
    templateDir.deleteRecursively()
}

fun goToUninitialized() {
    File(".github/workflows/initialize-project.yml").delete()
    File("README.md").delete()
    File("uninitialized.md").renameTo(File("README.md"))
}

if (args[0] == "a") {
    val repoDesc = args[1]
    val username = args[2]
    if (repoDesc.startsWith("app:") && repoDesc.contains(' ')) {
        val packageName = repoDesc.removePrefix("app:").split(' ').first()
        val appName = repoDesc.removePrefix("app:$packageName ")
        replaceName(packageName, appName, username)
        move()
    } else {
        goToUninitialized()
    }
} else {
    val packageName = args[1]
    val appName = args[2]
    val username = args[3]
    replaceName(packageName, appName, username)
    move()
}
