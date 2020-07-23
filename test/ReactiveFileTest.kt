import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.settingdust.laven.ReactiveFile.subscribe
import me.settingdust.laven.directory
import me.settingdust.laven.file
import me.settingdust.laven.isDirectory
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReactiveFileTest {
    @Test
    fun `path should be directory`() {
        val path = Paths.get("testresources", "reactive").directory
        assertTrue(path.isDirectory)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `file event should be published`() {
        val path = Paths.get("testresources", "reactive")
        var flag = 0
        path.subscribe {
            create {
                flag++
                assertEquals("", it.toFile().readText())
            }
            modify {
                flag++
                assertTrue(it.toFile().readText().isNotBlank())
            }
            delete {
                flag++
                assertFalse(it.toFile().exists())
            }
        }

        path.file.apply {
            createNewFile()
            writeText(Math.random().toString())
            delete()
        }
        GlobalScope.launch {
            assertEquals(3, flag)
        }
    }
}