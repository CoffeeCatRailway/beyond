package io.github.ocelot.beyond.client;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_COMPONENTS;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_BASE_LEVEL;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

@Mod.EventBusSubscriber(modid = Beyond.MOD_ID, value = Dist.CLIENT)
public class DebugInputs
{
    @SubscribeEvent
    public static void onEvent(InputEvent.KeyInputEvent event)
    {
        if (FMLLoader.isProduction() || Minecraft.getInstance().screen instanceof ChatScreen)
            return;
        if (event.getKey() == GLFW.GLFW_KEY_P)
        {
            try
            {
                Path outputFolder = Paths.get(Minecraft.getInstance().gameDirectory.toURI()).resolve("debug-out");
                if (!Files.exists(outputFolder))
                    Files.createDirectories(outputFolder);

                for (int i = 0; i < 1024; i++)
                {
                    if (!glIsTexture(i))
                        continue;

                    glBindTexture(GL_TEXTURE_2D, i);
                    int base = glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL);
                    int max = glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL);
                    if(max == 1000)
                        max = 0;

                    for (int level = base; level <= max; level++)
                    {
                        Path outputFile = outputFolder.resolve(i + (base == max ? "" : "-" + level) + ".png");
                        if (!Files.exists(outputFile))
                            Files.createFile(outputFile);

                        int width = glGetTexLevelParameteri(GL_TEXTURE_2D, level, GL_TEXTURE_WIDTH);
                        int height = glGetTexLevelParameteri(GL_TEXTURE_2D, level, GL_TEXTURE_HEIGHT);
                        int components = glGetTexLevelParameteri(GL_TEXTURE_2D, level, GL_TEXTURE_COMPONENTS);
                        int componentsCount = components == GL_RGB ? 3 : 4;

                        ByteBuffer image = BufferUtils.createByteBuffer(width * height * componentsCount);
                        glGetTexImage(GL_TEXTURE_2D, level, components, GL_UNSIGNED_BYTE, image);

                        Util.ioPool().execute(() -> stbi_write_png(outputFile.toString(), width, height, componentsCount, image, 0));
                    }
                }
                Util.getPlatform().openFile(outputFolder.toFile());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
