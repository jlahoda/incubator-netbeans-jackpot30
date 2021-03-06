/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jackpot30.ide.browsing;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.codeviation.pojson.Pojson;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.jackpot30.remoting.api.Utilities.RemoteSourceDescription;
import org.netbeans.modules.jackpot30.remoting.api.RemoteIndex;
import org.netbeans.modules.jackpot30.remoting.api.WebUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.UserQuestionException;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType = "text/x-rjava", service = HyperlinkProviderExt.class)
public class HyperlinkProviderImpl implements HyperlinkProviderExt {

    private static final Logger LOG = Logger.getLogger(HyperlinkProviderImpl.class.getName());

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        try {
            int[] span = Utilities.getIdentifierBlock((BaseDocument) doc, offset);

            return span;
        } catch (BadLocationException ex) {
            return null;
        }
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        RemoteSourceDescription rsd = org.netbeans.modules.jackpot30.remoting.api.Utilities.remoteSource(doc);
        FileObject file = NbEditorUtilities.getFileObject(doc);

        if (rsd != null && file != null) {
            try {
                URI sourceURI = new URI(rsd.idx.remote.toExternalForm() + "/ui/target?path=" + WebUtilities.escapeForQuery(rsd.idx.remoteSegment) + "&relative=" + WebUtilities.escapeForQuery(rsd.relative) + "&position=" + offset);
                Map<Object, Object> targetData = Pojson.load(HashMap.class, sourceURI.toURL().openStream());

                if (targetData.containsKey("position")) {
                    open(file, Integer.parseInt(String.valueOf(targetData.get("position"))));
                } else if (targetData.containsKey("source")) {
                    RemoteIndex targetIDX = null;
                    for (RemoteIndex i : RemoteIndex.loadIndices()) {
                        if (i.remote.equals(rsd.idx.remote) && i.remoteSegment.equals(targetData.get("path"))) {
                            targetIDX = i;
                        }
                    }

                    if (targetIDX == null) {
                        //TODO
                    } else {
                        String relativePath = (String) targetData.get("source");
                        URI declarationSpanURI = new URI(targetIDX.remote.toExternalForm() + "/ui/declarationSpan?path=" + WebUtilities.escapeForQuery(targetIDX.remoteSegment) + "&relative=" + WebUtilities.escapeForQuery(relativePath) + "&signature=" + WebUtilities.escapeForQuery(String.valueOf(targetData.get("signature"))));
                        List<Long> span = Pojson.update(new ArrayList<Long>(), declarationSpanURI.toURL().openStream());
                        //TODO: if the target is on disk, should use standard way to open:
                        open(targetIDX.getFile(relativePath), (int) (long) span.get(2));
                    }
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return null;
    }

    public @Deprecated static boolean open(final FileObject fo, final int offset) {
        return doOpen(fo, offset);
    }

    @Messages("TXT_Question=Question")
    private static boolean doOpen(FileObject fo, int offset) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
            LineCookie lc = od.getLookup().lookup(LineCookie.class);

            if (ec != null && lc != null && offset != -1) {
                StyledDocument doc = null;
                try {
                    doc = ec.openDocument();
                } catch (UserQuestionException uqe) {
                    final Object value = DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Confirmation(uqe.getLocalizedMessage(),
                            Bundle.TXT_Question(),
                            NotifyDescriptor.YES_NO_OPTION));
                    if (value != NotifyDescriptor.YES_OPTION) {
                        return false;
                    }
                    uqe.confirmed();
                    doc = ec.openDocument();
                }
                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;

                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);

                        if (l != null) {
                            doShow( l, column);
                            return true;
                        }
                    }
                }
            }

            OpenCookie oc = od.getLookup().lookup(OpenCookie.class);

            if (oc != null) {
                doOpen(oc);
                return true;
            }
        } catch (IOException e) {
            if (LOG.isLoggable(Level.INFO))
                LOG.log(Level.INFO, e.getMessage(), e);
        }

        return false;
    }

    private static void doShow(final Line l, final int column) {
        if (SwingUtilities.isEventDispatchThread()) {
            l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
                }
            });
        }
    }

    private static void doOpen(final OpenCookie oc) {
        if (SwingUtilities.isEventDispatchThread()) {
            oc.open();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    oc.open();
                }
            });
        }
    }
}
