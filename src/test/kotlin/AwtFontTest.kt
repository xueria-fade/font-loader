import org.junit.Test
import java.awt.Font
import java.awt.font.FontRenderContext

class AwtFontTest {

    @Test
    fun `test diff char ascent and descent`() {
        val font = Font("Microsoft YaHei", Font.PLAIN, 12)
        val context = FontRenderContext(font.transform, true, true)
        val test = "AcgH" + "学如爱己"
        test.map { char ->
            val line = font.getLineMetrics(char.toString(), context)
            return@map line.ascent to line.strikethroughOffset
        }.let(::println)
    }

}