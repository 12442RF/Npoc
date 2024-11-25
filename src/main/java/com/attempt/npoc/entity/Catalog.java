package com.attempt.npoc.entity;

import com.attempt.npoc.utils.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.attempt.npoc.utils.ConfigLoader.loadConfig;

public class Catalog {
    private final String templatesDirectory;
    private final Path templatesFS;

    // 构造函数
    public Catalog(String directory) throws IOException {
        if (directory != null && !directory.isEmpty()) {
            this.templatesDirectory = directory;
            this.templatesFS = Paths.get(directory);
        } else {
            Config config = loadConfig("./config/config.json");
            this.templatesDirectory = config.getTemplatesDirectory(); // 假设Config类已定义
            this.templatesFS = Paths.get(config.getTemplatesDirectory());
        }
    }
    // 打开文件并返回InputStream
    public InputStream openFile(String filename) throws IOException, IOException {
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            filePath = Paths.get(BackwardsCompatiblePaths(templatesDirectory, filename));
        }
        return Files.newInputStream(filePath);
    }
    // 处理向后兼容的路径
    private String BackwardsCompatiblePaths(String directory, String filename) {
        // 实现向后兼容的路径逻辑
        // 这里为了简化，直接返回组合路径
        return Paths.get(directory, filename).toString();
    }
    // 列出目录中的所有文件
    public Stream<Path> listFiles() throws IOException {
        return Files.walk(templatesFS);
    }
    public static void main(String[] args) {
        try {
            Catalog catalog = new Catalog("path/to/templates");

            // 打开文件
            try (InputStream inputStream = catalog.openFile("path/to/file.txt")) {
                // 读取文件内容
                byte[] content = inputStream.readAllBytes();
                System.out.println(new String(content));
            }

            // 列出目录中的所有文件
            try (Stream<Path> paths = catalog.listFiles()) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> System.out.println(path.toString()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
