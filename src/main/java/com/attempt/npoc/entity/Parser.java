package com.attempt.npoc.entity;

import com.attempt.npoc.utils.Cache;
import com.attempt.npoc.utils.ParserError;
import com.attempt.npoc.utils.TemplateErrors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.attempt.npoc.http.RawHttp;
import lombok.Data;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class Parser {
    static final Logger logger = Logger.getLogger(RawHttp.class);
    private boolean shouldValidate;
    private boolean noStrictSyntax;
    private Cache parsedTemplatesCache;
    private  Cache compiledTemplatesCache;
    
    
    public Parser() {
        this.parsedTemplatesCache = new Cache();
        this.compiledTemplatesCache = new Cache();
    }
    
    public Cache getCache() {
        return parsedTemplatesCache;
    }
    
    public void loadTemplate(String templatePathDir, Catalog catalog) throws Exception {
        List<String> yamlFiles = findYamlFiles(templatePathDir);
        for (String yamlFile : yamlFiles) {
//            logger.info("Loading template: " + yamlFile);
            Template template = parseTemplate(yamlFile, catalog);
            List<Exception> validationError = validateTemplateMandatoryFields(template);
//            if (template.getFlow() != null && !template.getFlow().isEmpty()) {
//                return false;
//            }
            if (validationError != null) {
                for (Exception e : validationError) {
                    // 处理每个异常，例如打印异常信息
                    logger.error(e.getMessage());
                }
            }
            
        }
    }
    
    public Template parseTemplate(String templatePath, Catalog catalog) throws Exception {

        ParsedTemplate cachedTemplate = parsedTemplatesCache.has(templatePath);
        if (cachedTemplate != null) {
            return cachedTemplate.getTemplate();
        }
        InputStream reader = ReaderFromPath(templatePath, catalog);
        
        byte[] data = reader.readAllBytes();
        Template template = new Template();

        String extension = getFileExtension(templatePath);
        switch (extension) {
            case "yaml":
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                template = mapper.readValue(new File(templatePath), Template.class);
                break;
            default:
                throw new Exception("Failed to identify template format expected JSON or YAML but got: " + templatePath);
        }
        parsedTemplatesCache.store(templatePath, new ParsedTemplate(template,new String(data),null,templatePath));
        return template;
    }

    private InputStream ReaderFromPath(String templatePath, Catalog catalog) throws IOException {
        InputStream inputStream = catalog.openFile(templatePath);
        return inputStream;
    }

    public boolean loadWorkflow(String templatePath, Catalog catalog) throws Exception {
        Template template = parseTemplate(templatePath, catalog);
        if (template != null && template.getFlow() != null && !template.getFlow().isEmpty()) {
            List<Exception> validationError = validateTemplateMandatoryFields(template);
            if (validationError != null) {
                for (Exception e : validationError) {
                    // 处理每个异常，例如打印异常信息
                    logger.error(e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    private String getFileExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        return (dotIndex == -1) ? "" : path.substring(dotIndex + 1);
    }

    private List<Exception> validateTemplateMandatoryFields(Template template) {
        // Implement validation logic here
        Model info = template.getInfo();
        List<Exception> validateErrors = new ArrayList<>();
        if (info.getName() == null || info.getName().isEmpty()) {
            validateErrors.add(ParserError.newWithFmt(TemplateErrors.ErrMandatoryFieldMissingFmt,"name"));
        }
        if (info.getAuthor().isEmpty()){
            validateErrors.add(ParserError.newWithFmt(TemplateErrors.ErrMandatoryFieldMissingFmt,"author"));
        }
        if (Objects.equals(template.getId(), "")){
            validateErrors.add(ParserError.newWithFmt(TemplateErrors.ErrMandatoryFieldMissingFmt,"id"));
        } else if (!template.getId().matches("^([a-zA-Z0-9]+[-_])*[a-zA-Z0-9]+$")) {
            validateErrors.add(ParserError.newWithFmt(TemplateErrors.ErrInvalidField,"id","^([a-zA-Z0-9]+[-_])*[a-zA-Z0-9]+$"));
        }
        if (validateErrors.size() > 0)  {
            return validateErrors;
        }else {
            return null;
        }
    }

    private Exception validateTemplateOptionalFields(Template template) {
        // Implement validation logic here
        return null;
    }

    private boolean isTemplateInfoMetadataMatch(TagFilter tagFilter, Template template, List<String> extraTags) {
        // Implement matching logic here
        return true;
    }
    public  List<String> findYamlFiles(String directoryPath) throws IOException {
        List<String> yamlFiles = new ArrayList<>();
        Path startDir = Paths.get(directoryPath);

        // 使用 Files.walk 遍历目录
        Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".yaml")) {
//                    logger.info("Found YAML file: " + file);
                    yamlFiles.add(file.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return yamlFiles;
    }
}
