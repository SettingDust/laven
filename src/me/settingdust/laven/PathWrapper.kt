package me.settingdust.laven

import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute

val Path.absolutePath: Path
    get() = this.toAbsolutePath()
val Path.isDirectory
    get() = Files.isDirectory(this)
val Path.directory: Path
    get() = if (this.isDirectory) this else this.parent
val Path.file: File
    get() = toFile()