import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import kotlin.test.Test
import kotlin.test.assertEquals

class QualifiedNameTest {
    @Test
    fun qualifiedNameTest() {
        assertEquals("com.github.xuankaicat.xhys.ksp.annotation.Behaviour", Behaviour::class.qualifiedName!!)
    }
}

