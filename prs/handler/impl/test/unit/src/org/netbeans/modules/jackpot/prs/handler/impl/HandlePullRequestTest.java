/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jackpot.prs.handler.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class HandlePullRequestTest extends NbTestCase {
    
    public HandlePullRequestTest(String name) {
        super(name);
    }
    
    @Test
    public void testDiffParse() throws Exception {
        clearWorkDir();
        var workDir = FileUtil.toFileObject(getWorkDir());
        FileUtil.createData(workDir, ".project");
        var flow = FileUtil.createData(workDir, "java/java.hints/src/org/netbeans/modules/java/hints/introduce/Flow.java");
        try (Writer w = new OutputStreamWriter(flow.getOutputStream())) {
            for (int i = 0; i < 1500; i++) {
                w.append("\n");
            }
        }
        var diff = HandlePullRequestTest.class.getResource("35.diff");
        var remaps = HandlePullRequest.parseDiff(diff.toString(), workDir).second();
        assertEquals(6, remaps.get(flow).remap[105]);
        assertEquals(30, remaps.get(flow).remap[455]);
        assertEquals(53, remaps.get(flow).remap[477]);
        assertEquals(71, remaps.get(flow).remap[1210]);
        assertEquals(82, remaps.get(flow).remap[1220]);
        assertEquals(90, remaps.get(flow).remap[1228]);
        assertEquals(123, remaps.get(flow).remap[1259]);
    }

    @ServiceProvider(service=ProjectFactory.class)
    public static final class ProjectFactoryImpl implements ProjectFactory {

        @Override
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getFileObject(".project") != null;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory)) {
                return new Project() {
                    @Override
                    public FileObject getProjectDirectory() {
                        return projectDirectory;
                    }
                    @Override
                    public Lookup getLookup() {
                        return Lookup.EMPTY;
                    }
                };
            } else {
                return null;
            }
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
        }

    }
}
