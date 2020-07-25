import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import me.settingdust.laven.FileEvent
import me.settingdust.laven.ReactiveFile.subscribe
import me.settingdust.laven.absolutePath
import me.settingdust.laven.directory
import me.settingdust.laven.file
import me.settingdust.laven.isDirectory
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ReactiveFileTest {
    @Test
    fun `path should be directory`() {
        val path = Paths.get("testresources", "reactive").directory
        assertTrue(path.isDirectory)
    }

    @Test
    fun `file create event should be fired`() {
        var receive: FileEvent<String?>?
        val path = Paths.get("testresources", "create")
        path.file.delete()
        val channel = path.subscribe<String?> {
            converter { path, kind ->
                if (kind != FileEvent.Kind.Delete) {
                    return@converter path.file.readText()
                } else {
                    return@converter null
                }
            }
        }
        path.file.createNewFile()
        runBlocking {
            withTimeout(5000) {
                receive = channel.receive()
            }
        }
        assertNotNull(receive)
        assertEquals(path.absolutePath, receive!!.path)
        assertEquals(FileEvent.Kind.Create, receive!!.kind)
        assertNotNull(receive!!.data)
        assert(receive!!.data!!.isBlank())
    }

    @Test
    fun `file modify event should be fired`() {
        val random = Math.random().toString()
        val path = Paths.get("testresources", "modify")
        var receive: FileEvent<String?>?
        path.file.createNewFile()
        val channel = path.subscribe<String?> {
            converter { path, kind ->
                if (kind != FileEvent.Kind.Delete) {
                    return@converter path.file.readText()
                } else {
                    return@converter null
                }
            }
        }
        path.file.writeText(random)
        runBlocking {
            withTimeout(5000) {
                receive = channel.receive()
            }
        }
        assertNotNull(receive)
        assertEquals(path.absolutePath, receive!!.path)
        assertEquals(FileEvent.Kind.Modify, receive!!.kind)
        assertNotNull(receive!!.data)
        assertEquals(random, receive!!.data)
    }

    @Test
    fun `file delete event should be fired`() {
        var receive: FileEvent<String?>?
        val path = Paths.get("testresources", "delete")
        path.file.createNewFile()
        val channel = path.subscribe<String?> {
            converter { path, kind ->
                if (kind != FileEvent.Kind.Delete) {
                    return@converter path.file.readText()
                } else {
                    return@converter null
                }
            }
        }
        path.file.delete()
        runBlocking {
            withTimeout(5000) {
                receive = channel.receive()
            }
        }
        assertNotNull(receive)
        assertEquals(path.absolutePath, receive!!.path)
        assertEquals(FileEvent.Kind.Delete, receive!!.kind)
        assertNull(receive!!.data)
    }
}