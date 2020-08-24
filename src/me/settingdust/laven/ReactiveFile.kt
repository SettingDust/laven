package me.settingdust.laven

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchEvent

@ExperimentalCoroutinesApi
object ReactiveFile {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val pathToHandlerMap = mutableMapOf<Path, FileEventChannel<*>>()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val watchKey = watchService.take()
                val path = watchKey.watchable()
                if (path is Path) {
                    val pollEvents = watchKey.pollEvents()
                    val events = pollEvents.reversed().distinctBy { it.context() }.reversed()
                    events.forEach {
                        val currentPath = path.resolve(it.context() as Path).absolutePath
                        val kind = FileEvent.Kind.getByKind(it.kind())
                        if (kind != null) {
                            @Suppress("UNCHECKED_CAST")
                            val fileEventChannel = pathToHandlerMap[currentPath] as? FileEventChannel<Any?>
                            fileEventChannel?.apply {
                                if (isClosedForSend) {
                                    pathToHandlerMap.remove(currentPath)
                                } else {
                                    send(
                                        FileEvent(
                                            path = currentPath,
                                            kind = kind,
                                            data = converter?.invoke(currentPath, kind)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (!watchKey.reset() || path !is Path) {
                    watchKey.cancel()
                    continue
                }
            }
        }
    }

    fun <T> Path.subscribe(fileEventChannel: FileEventChannel<T>.() -> Unit): FileEventChannel<T> {
        val channel = FileEventChannel<T>()
        fileEventChannel(channel)
        this.subscribe(channel)
        return channel
    }

    fun <T> Path.subscribe(fileEventChannel: FileEventChannel<T>): FileEventChannel<T> {
        val path = this.absolutePath
        path.directory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        pathToHandlerMap[path] = fileEventChannel
        return fileEventChannel
    }
}

open class FileEventChannel<T>(
    private val channel: Channel<FileEvent<T>> = Channel()
) : Channel<FileEvent<T>> by channel {
    internal var converter: ((path: Path, kind: FileEvent.Kind) -> T)? = null
    fun converter(consumer: (path: Path, kind: FileEvent.Kind) -> T) {
        this.converter = consumer
    }
}

data class FileEvent<T>(
    /**
     * Path of file/directory
     */
    val path: Path,

    /**
     * Kind of event
     */
    val kind: Kind,
    val data: T
) {
    enum class Kind(val kind: WatchEvent.Kind<Path>?) {
        Create(ENTRY_CREATE), Modify(ENTRY_MODIFY), Delete(ENTRY_DELETE);

        companion object {
            fun getByKind(kind: WatchEvent.Kind<*>) = values()
                .filter { it.kind != null }
                .singleOrNull { it.kind!! == kind }
        }
    }
}
