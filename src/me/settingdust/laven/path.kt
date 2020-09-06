package me.settingdust.laven

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute

val Path.absolutePath: Path
    get() = this.toAbsolutePath()
val Path.isDirectory
    get() = Files.isDirectory(this)
val Path.directory: Path
    get() = if (this.isDirectory) this else this.parent
val Path.exist: Boolean
    get() = Files.exists(this)
val Path.file: File
    get() = toFile()

fun Path.createDirectories(vararg attr: FileAttribute<Any>): Path = Files.createDirectories(directory, *attr)

fun Path.createFile(vararg attr: FileAttribute<Any>): Path = Files.createFile(this, *attr)

fun Path.openInputStream(vararg option: OpenOption): InputStream = Files.newInputStream(this, *option)