import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class CodecTest
{
    public static void main(String[] args)
    {
        Foo foo = new Foo(new String[]{"test1", "test2"});
        System.out.println(Foo.CODEC.encodeStart(JsonOps.INSTANCE, foo).getOrThrow(false, System.err::println));
    }

    private static class Foo
    {
        public static final Codec<Foo> CODEC = Codec.STRING.listOf().fieldOf("values").xmap(values -> new Foo(values.toArray(new String[0])), foo -> Arrays.asList(foo.getValues())).codec();

        private final String[] values;

        private Foo(String[] values)
        {
            this.values = values;
        }

        public String[] getValues()
        {
            return values;
        }
    }
}
