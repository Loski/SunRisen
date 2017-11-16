package fr.upmc.javassist;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

public abstract class TemplateOutboundPort extends AbstractOutboundPort {

	public				TemplateOutboundPort(
			ComponentI owner
			) throws Exception
		{
			super(TemplateOutboundPort.class, owner) ;
		}

		public				TemplateOutboundPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, TemplateOutboundPort.class, owner);
		}
}
