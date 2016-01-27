package com.millennialmedia.intellibot.psi.element;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.millennialmedia.intellibot.psi.RobotFeatureFileType;
import com.millennialmedia.intellibot.psi.RobotLanguage;
import com.millennialmedia.intellibot.psi.dto.ImportType;
import com.millennialmedia.intellibot.psi.util.PerformanceCollector;
import com.millennialmedia.intellibot.psi.util.PerformanceEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * @author Stephen Abrams
 */
public class RobotFileImpl extends PsiFileBase implements RobotFile, KeywordFile, PerformanceEntity {

    private Collection<Heading> headings;

    public RobotFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, RobotLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return RobotFeatureFileType.getInstance();
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        this.headings = null;
    }

    @NotNull
    @Override
    public Collection<DefinedVariable> getDefinedVariables() {
        Collection<DefinedVariable> results = new ArrayList<DefinedVariable>();
        for (Heading heading : getHeadings()) {
            results.addAll(heading.getDefinedVariables());
        }
        return results;
    }

    @NotNull
    @Override
    public ImportType getImportType() {
        return ImportType.RESOURCE;
    }

    @NotNull
    @Override
    public Collection<DefinedKeyword> getDefinedKeywords() {
        Collection<DefinedKeyword> results = new ArrayList<DefinedKeyword>();
        for (Heading heading : getHeadings()) {
            results.addAll(heading.getDefinedKeywords());
        }
        return results;
    }

    @NotNull
    @Override
    public Collection<PsiFile> getFilesFromInvokedKeywordsAndVariables() {
        Collection<PsiFile> results = new HashSet<PsiFile>();
        for (Heading heading : getHeadings()) {
            results.addAll(heading.getFilesFromInvokedKeywordsAndVariables());
        }
        return results;
    }

    @NotNull
    @Override
    public Collection<KeywordFile> getImportedFiles(boolean includeTransitive) {
        Collection<KeywordFile> results = new LinkedHashSet<KeywordFile>();
        for (Heading heading : getHeadings()) {
            for (KeywordFile file : heading.getImportedFiles()) {
                addKeywordFiles(results, file, includeTransitive);
            }
        }
        return results;
    }

    private void addKeywordFiles(Collection<KeywordFile> files, KeywordFile current, boolean includeTransitive) {
        if (files.add(current)) {
            if (includeTransitive) {
                for (KeywordFile file : current.getImportedFiles(false)) {
                    addKeywordFiles(files, file, true);
                }
            }
        }
    }

    @Override
    public void importsChanged() {
        for (Heading heading : getHeadings()) {
            heading.importsChanged();
        }
    }

    @NotNull
    @Override
    public Collection<KeywordInvokable> getKeywordReferences(@Nullable KeywordDefinition definition) {
        Collection<KeywordInvokable> results = new ArrayList<KeywordInvokable>();
        for (Heading heading : getHeadings()) {
            results.addAll(heading.getKeywordReferences(definition));
        }
        return results;
    }

    @NotNull
    private Collection<Heading> getHeadings() {
        Collection<Heading> results = this.headings;
        if (results == null) {
            PerformanceCollector debug = new PerformanceCollector(this, "headings");
            results = collectHeadings();
            this.headings = results;
            debug.complete();
        }
        return results;
    }

    @NotNull
    private Collection<Heading> collectHeadings() {
        Collection<Heading> results = new ArrayList<Heading>();
        for (PsiElement child : getChildren()) {
            if (child instanceof Heading) {
                results.add((Heading) child);
            }
        }
        return results;
    }

    @NotNull
    @Override
    public String getDebugFileName() {
        return getVirtualFile().getName();
    }

    @NotNull
    @Override
    public String getDebugText() {
        return ".";
    }
}
