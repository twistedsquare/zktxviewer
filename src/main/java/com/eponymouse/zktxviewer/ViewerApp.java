/*
 * Copyright 2021 Neil Brown.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.eponymouse.zktxviewer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import org.lwjgl.opengl.GL30;

import java.io.File;

public class ViewerApp extends ApplicationAdapter
{
    private final File file;
    private Texture texture;
    private ShaderProgram shader;
    private ImmediateModeRenderer renderer;
    private int totalLevels;
    // -1 means automatic, 0+ means specific level
    private int mipMapLevel;

    public ViewerApp(File file)
    {
        this.file = file;
    }

    @Override
    public void create()
    {
        KTXTextureData data = new KTXTextureData(Gdx.files.absolute(file.getAbsolutePath()), false);
        texture = new Texture(data);
        totalLevels = data.getNumberOfMipMapLevels();
        shader = new ShaderProgram(Gdx.files.internal("vertex.glsl"), Gdx.files.internal("fragment.glsl"));
        renderer = new ImmediateModeRenderer20(10, false, false, 1, shader);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode)
            {
                if (keycode == Keys.RIGHT)
                {
                    mipMapLevel = Math.min(mipMapLevel + 1, totalLevels - 1);
                }
                else if (keycode == Keys.LEFT)
                {
                    mipMapLevel = Math.max(mipMapLevel - 1, -1);
                }
                return super.keyDown(keycode);
            }
        });
    }

    @Override
    public void render()
    {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        setupViewport();
        System.out.println("Level: " + mipMapLevel);
        Gdx.gl.glTexParameteri(texture.glTarget, GL30.GL_TEXTURE_BASE_LEVEL, mipMapLevel == -1 ? 0 : mipMapLevel);
        Gdx.gl.glTexParameteri(texture.glTarget, GL30.GL_TEXTURE_MAX_LEVEL, mipMapLevel == -1 ? totalLevels : mipMapLevel);
        texture.bind(0);
        renderer.begin(new Matrix4(), GL20.GL_TRIANGLE_FAN);
        renderer.texCoord(0, 0);
        renderer.vertex(-1, -1, 0);
        renderer.texCoord(1, 0);
        renderer.vertex(1, -1, 0);
        renderer.texCoord(1, 1);
        renderer.vertex(1, 1, 0);
        renderer.texCoord(0, 1);
        renderer.vertex(-1, 1, 0);
        renderer.end();
    }

    private void setupViewport()
    {
        float widthIfFullHeight = (float)Gdx.graphics.getHeight() / texture.getHeight()  * texture.getWidth();
        float heightIfFullWidth = (float)Gdx.graphics.getWidth() / texture.getWidth() * texture.getHeight();
        if (widthIfFullHeight > Gdx.graphics.getWidth())
        {
            // Width-limited:
            Gdx.gl.glViewport(0, (int)(Gdx.graphics.getHeight() - heightIfFullWidth) / 2, Gdx.graphics.getWidth(), (int)heightIfFullWidth);
        }
        else
        {
            // Height-limited:
            Gdx.gl.glViewport((int)(Gdx.graphics.getWidth() - widthIfFullHeight) / 2, 0, (int)widthIfFullHeight, Gdx.graphics.getHeight());
        }
    }
}
