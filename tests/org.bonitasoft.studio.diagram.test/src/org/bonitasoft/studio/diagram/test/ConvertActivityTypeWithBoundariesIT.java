/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.diagram.test;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.bonitasoft.studio.common.jface.FileActionDialog;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.SendTask;
import org.bonitasoft.studio.model.process.ServiceTask;
import org.bonitasoft.studio.properties.i18n.Messages;
import org.bonitasoft.studio.swtbot.framework.diagram.BotProcessDiagramPerspective;
import org.bonitasoft.studio.test.swtbot.util.SWTBotTestUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ConvertActivityTypeWithBoundariesIT extends SWTBotGefTestCase {

    private final static String STEP_WITH_BOUNDARY_NAME = "Step1";

    boolean disablePopupState;
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        disablePopupState = FileActionDialog.getDisablePopup();
        FileActionDialog.setDisablePopup(true);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileActionDialog.setDisablePopup(disablePopupState);
    }

    @Test
    public void testConvertWithRemovedTimerBoundary() throws Exception {
        final MainProcess diagram = importAndConvertStep1ToServiceTask(
                "TestConvertActivtyWithTimerBoundary-1.0.bos",
                "TestConvertActivtyWithTimerBoundary");
        final Pool pool = (Pool) diagram.getElements().get(0);
        Assertions.assertThat(pool.getConnections()).hasSize(0);
        final ServiceTask serviceTask = (ServiceTask) pool.getElements().get(1);
        Assertions.assertThat(serviceTask.getName()).isEqualTo(STEP_WITH_BOUNDARY_NAME);
        Assertions.assertThat(serviceTask.getBoundaryIntermediateEvents()).hasSize(0);
    }

    @Test
    public void testConvertWithRemovedMessageBoundary() throws Exception {
        final MainProcess diagram = importAndConvertStep1ToServiceTask(
                "TestConvertActivityWithMessageBoundary-1.0.bos",
                "TestConvertActivityWithMessageBoundary");
        final Pool pool = (Pool) diagram.getElements().get(0);
        Assertions.assertThat(pool.getConnections()).hasSize(0);
        final ServiceTask serviceTask = (ServiceTask) pool.getElements().get(1);
        Assertions.assertThat(serviceTask.getName()).isEqualTo(STEP_WITH_BOUNDARY_NAME);
        Assertions.assertThat(serviceTask.getBoundaryIntermediateEvents()).hasSize(0);

        final Pool senderMessagePool = (Pool) diagram.getElements().get(1);
        Assertions.assertThat(((SendTask) senderMessagePool.getElements().get(0)).getOutgoingMessages()).isEmpty();
    }

    @Test
    public void testConvertKeepingBoundary() throws Exception {
        final MainProcess diagram = importAndConvertStep1ToServiceTask(
                "TestConvertActivityTypeWithCompatibleBoundary-1.0.bos",
                "TestConvertActivityTypeWithCompatibleBoundary");
        final Pool pool = (Pool) diagram.getElements().get(0);
        Assertions.assertThat(pool.getConnections()).hasSize(1);
        final ServiceTask serviceTask = (ServiceTask) pool.getElements().get(1);
        Assertions.assertThat(serviceTask.getName()).isEqualTo(STEP_WITH_BOUNDARY_NAME);
        Assertions.assertThat(serviceTask.getBoundaryIntermediateEvents()).hasSize(1);
    }

    protected MainProcess importAndConvertStep1ToServiceTask(final String resourceNameInClasspath, final String diagramEditorTitle) throws IOException {
        SWTBotTestUtil.importProcessWIthPathFromClass(bot,
                resourceNameInClasspath,
                SWTBotTestUtil.IMPORTER_TITLE_BONITA,
                diagramEditorTitle,
                this.getClass(),
                false);
        final BotProcessDiagramPerspective botProcessDiagramPerspective = new BotProcessDiagramPerspective(bot);
        botProcessDiagramPerspective.activeProcessDiagramEditor().selectElement(STEP_WITH_BOUNDARY_NAME);
        botProcessDiagramPerspective.getDiagramPropertiesPart().selectGeneralTab().selectGeneralTab().setTaskType(Messages.activityType_serviceTask);
        botProcessDiagramPerspective.activeProcessDiagramEditor().getGmfEditor().save();

        final SWTBotGefEditPart mainEditPart = botProcessDiagramPerspective.activeProcessDiagramEditor().getGmfEditor().mainEditPart();
        return (MainProcess) ((DiagramEditPart) (mainEditPart.part())).resolveSemanticElement();
    }

}
