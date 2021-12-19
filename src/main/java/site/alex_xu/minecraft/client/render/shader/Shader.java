package site.alex_xu.minecraft.client.render.shader;

import org.joml.Matrix4f;
import site.alex_xu.minecraft.client.render.Freeable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class Shader extends Freeable {
    private final int programID;
    private final ArrayList<Integer> shaderIds = new ArrayList<>();
    private final HashMap<String, Integer> uniformLocationMap = new HashMap<>();
    private boolean linked = false;

    public Shader() {
        programID = glCreateProgram();
        if (programID == 0) {
            getLogger().error("Unable to create shader program!");
            throw new IllegalStateException("Unable to create shader program!");
        }
    }

    public int getUniformLocation(String uniformName) {
        if (!uniformLocationMap.containsKey(uniformName)) {
            uniformLocationMap.put(uniformName, glGetUniformLocation(programID, uniformName));
        }
        int location = uniformLocationMap.get(uniformName);
        if (location == -1)
            throw new IllegalStateException("No uniform named: " + uniformName);
        return location;
    }

    public boolean hasUniform(String name) {
        if (!uniformLocationMap.containsKey(name)) {
            uniformLocationMap.put(name, glGetUniformLocation(programID, name));
        }
        int location = uniformLocationMap.get(name);

        return location != -1;
    }

    // Setters
    public Shader setInt(String uniformName, int value) {
        bind();
        glUniform1i(getUniformLocation(uniformName), value);
        return this;
    }

    public Shader setFloat(String uniformName, float value) {
        bind();
        glUniform1f(getUniformLocation(uniformName), value);
        return this;
    }

    public Shader setVec2(String uniformName, float x, float y) {
        bind();
        glUniform2f(getUniformLocation(uniformName), x, y);
        return this;
    }

    public Shader setVec3(String uniformName, float x, float y, float z) {
        bind();
        glUniform3f(getUniformLocation(uniformName), x, y, z);
        return this;
    }

    public Shader setVec4(String uniformName, float x, float y, float z, float w) {
        bind();
        glUniform4f(getUniformLocation(uniformName), x, y, z, w);
        return this;
    }

    public Shader setMat4(String uniformName, boolean transpose, Matrix4f mat4) {
        bind();
        glUniformMatrix4fv(getUniformLocation(uniformName), transpose, mat4.get(new float[16]));
        return this;
    }

    // Constructing

    private void addShader(int type, String source, String path) {
        int shaderID = glCreateShader(type);
        if (shaderID == 0) {
            getLogger().error("Unable to create shader!");
            throw new IllegalStateException("Unable to create shader!");
        }

        String shaderTypeName = type == GL_VERTEX_SHADER ? "vertex" : "fragment";

        glShaderSource(shaderID, source);
        glCompileShader(shaderID);

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            getLogger().error("Failed to compile: " + path);
            String reason = "Failed to compile " + shaderTypeName + " shader: " + glGetShaderInfoLog(shaderID);
            getLogger().error(reason);
            glDeleteShader(shaderID);
            throw new RuntimeException(reason);
        }

        glAttachShader(programID, shaderID);
        shaderIds.add(shaderID);
    }

    public Shader link() {
        if (linked) {
            throw new IllegalStateException("Cannot link program twice!");
        }
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            String reason = "Failed to link shader program: " + glGetProgramInfoLog(programID);
            getLogger().error(reason);
            throw new IllegalStateException(reason);
        }
        for (Integer id : this.shaderIds) {
            glDeleteShader(id);
        }
        linked = true;
        return this;
    }

    public Shader addFromResource(String filename) {
        if (linked) {
            throw new IllegalStateException("This shader program is already linked!");
        }
        if (filename.endsWith(".vert") || filename.endsWith(".frag")) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
            if (inputStream == null) {
                getLogger().error("Can't find shader from: " + filename);
                throw new RuntimeException("Can't find shader from: " + filename);
            }
            try {
                String source = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                inputStream.close();
                addShader(filename.endsWith(".vert") ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER, source, filename);
                return this;
            } catch (IOException e) {
                e.printStackTrace();
            }
            getLogger().error("Unable load shader source: " + filename);
            throw new RuntimeException("Unable load shader source: " + filename);
        } else {
            getLogger().error("Unable to determine shader type: extension should either be .vert or .frag!");
            throw new IllegalStateException("Unable to determine shader type: extension should either be .vert or .frag!");
        }
    }

    public Shader addFromPath(String filename) {
        if (linked) {
            throw new IllegalStateException("This shader program is already linked!");
        }
        if (filename.endsWith(".vert") || filename.endsWith(".frag")) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(filename);
            } catch (FileNotFoundException ignored) {
            }
            if (inputStream == null) {
                getLogger().error("Can't find shader from: " + filename);
                throw new RuntimeException("Can't find shader from: " + filename);
            }
            try {
                String source = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                inputStream.close();
                addShader(filename.endsWith(".vert") ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER, source, filename);
                return this;
            } catch (IOException e) {
                e.printStackTrace();
            }
            getLogger().error("Unable load shader source: " + filename);
            throw new RuntimeException("Unable load shader source: " + filename);
        } else {
            getLogger().error("Unable to determine shader type: extension should either be .vert or .frag!");
            throw new IllegalStateException("Unable to determine shader type: extension should either be .vert or .frag!");
        }
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    @Override
    protected void onDispose() {
        glDeleteProgram(programID);
    }
}
