/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.contract.core.validation;

import org.bonitasoft.studio.common.jface.databinding.MultiValidator;
import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
import org.bonitasoft.studio.common.jface.databinding.validator.InputLengthValidator;
import org.bonitasoft.studio.contract.i18n.Messages;
import org.bonitasoft.studio.model.process.ContractConstraint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;


/**
 * @author Romain Bioteau
 *
 */
public class ContractConstraintNameValidationRule extends MessageValidationRule implements IValidationRule {

    private static final int MAX_NAME_LENGTH = 50;

    protected static final String NAME_CONSTRAINT_ID = "constraint_name";

    private final MultiValidator multiValidator;

    public ContractConstraintNameValidationRule() {
        multiValidator = new MultiValidator();
        multiValidator.addValidator(new EmptyInputValidator(Messages.name));
        multiValidator.addValidator(new InputLengthValidator(Messages.name, MAX_NAME_LENGTH));
    }

    @Override
    public boolean appliesTo(final EObject element) {
        return element instanceof ContractConstraint;
    }

    @Override
    public IStatus validate(final EObject element) {
        final ContractConstraint input = (ContractConstraint) element;
        return multiValidator.validate(input.getName());
    }

    @Override
    public String getRuleId() {
        return NAME_CONSTRAINT_ID;
    }


}