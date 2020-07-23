package me.settingdust.laven

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.io.Closeable
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchEvent
import java.nio.file.WatchKey

@ExperimentalCoroutinesApi
object ReactiveFile {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val pathToHandlerMap = mutableMapOf<Path, Pair<WatchKey, FileEventHandler>>()
    private var channel = Channel<FileEvent>()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            channel.consumeEach {
                it.apply {
                    val pair = pathToHandlerMap[path]
                    val fileEventHandler = pair?.second
                    fileEventHandler?.apply {
                        if (closed) {
                            pathToHandlerMap.remove(path)
                        } else {
                            when (kind) {
                                FileEvent.Kind.Create -> create?.invoke(path)
                                FileEvent.Kind.Modify -> modify?.invoke(path)
                                FileEvent.Kind.Delete -> delete?.invoke(path)
                            }
                        }
                    }
                }
            }
            while (true) {
                val watchKey = watchService.take()
                val path = watchKey.watchable()
                if (path is Path) {
                    watchKey.pollEvents().forEach {
                        val currentPath = path.resolve(it.context() as Path).absolutePath
                        val kind = FileEvent.Kind.getByKind(it.kind())
                        if (kind != null) {
                            channel.send(
                                FileEvent(
                                    path = currentPath,
                                    kind = kind
                                )
                            )
                        }
                    }
                } else {
                    watchKey.cancel()
                    continue
                }

                if (!watchKey.reset()) {
                    watchKey.cancel()
                    continue
                }
            }
        }
    }

    fun Path.subscribe(consumer: FileEventHandler.() -> Unit) {
        val path = this.directory.absolutePath
        val key = path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        val handler = FileEventHandler()
        consumer(handler)

        pathToHandlerMap[path] = key to handler
    }
}

class FileEventHandler : Closeable {
    internal var create: ((path: Path) -> Unit)? = null
    internal var modify: ((path: Path) -> Unit)? = null
    internal var delete: ((path: Path) -> Unit)? = null
    internal var closed: Boolean = false
    fun create(consumer: (path: Path) -> Unit) {
        checkClosed()
        this.create = consumer
    }

    fun modify(consumer: (path: Path) -> Unit) {
        checkClosed()
        this.modify = consumer
    }

    fun delete(consumer: (path: Path) -> Unit) {
        checkClosed()
        this.delete = consumer
    }

    /**
     * Close this handler
     */
    override fun close() {
        checkClosed()
        this.closed = true
    }

    private fun checkClosed() {
        if (closed) throw ClosedReceiveChannelException("Closed file event handler")
    }
}

data class FileEvent(
    /**
     * Path of file/directory
     */
    val path: Path,

    /**
     * Kind of event
     */
    val kind: Kind,
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
