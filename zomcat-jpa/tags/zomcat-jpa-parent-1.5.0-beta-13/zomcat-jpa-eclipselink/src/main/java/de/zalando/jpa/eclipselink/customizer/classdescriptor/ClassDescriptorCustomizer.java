package de.zalando.jpa.eclipselink.customizer.classdescriptor;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.sessions.Session;

/**
 * Defines an {@link ClassDescriptorCustomizer}.<br/>
 * With an {@link ClassDescriptor} we can customize field-and association-mappings for one class/entity/type.
 *
 * @author  jbellmann
 */
public interface ClassDescriptorCustomizer {

    void customize(ClassDescriptor clazzDescriptor, Session session);

}
