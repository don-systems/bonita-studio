/*
 * generated by Xtext
 */
package org.bonitasoft.studio.condition;

import org.bonitasoft.studio.condition.scoping.ConditionModelGlobalScopeProvider;
import org.eclipse.xtext.resource.IContainer.Manager;
import org.eclipse.xtext.resource.impl.SimpleResourceDescriptionsBasedContainerManager;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class ConditionModelRuntimeModule extends org.bonitasoft.studio.condition.AbstractConditionModelRuntimeModule {

	@Override
	public Class<? extends org.eclipse.xtext.scoping.IGlobalScopeProvider> bindIGlobalScopeProvider() {
		return ConditionModelGlobalScopeProvider.class;
	}

	@Override
	public Class<? extends Manager> bindIContainer$Manager() {
		return SimpleResourceDescriptionsBasedContainerManager.class;
	}

	
}
