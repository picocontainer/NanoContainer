package org.nanocontainer.reflection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.nanocontainer.integrationkit.ContainerPopulator;
import org.nanocontainer.integrationkit.ContainerRecorder;
import org.nanocontainer.script.xml.XMLContainerBuilder;
import org.nanocontainer.testmodel.FredImpl;
import org.nanocontainer.testmodel.ThingThatTakesParamsInConstructor;
import org.nanocontainer.testmodel.WilmaImpl;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.defaults.ComponentParameter;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @author Konstantin Pribluda ( konstantin.pribluda(at)infodesire.com )
 * @author Aslak Helles&oslash;y
 */
public class DefaultContainerRecorderTestCase extends TestCase {
    public void testInvocationsCanBeRecordedAndReplayedOnADifferentContainerInstance() throws Exception {
        ContainerRecorder recorder = new DefaultContainerRecorder(new DefaultNanoPicoContainer());
        MutablePicoContainer recorded = recorder.getContainerProxy();

        recorded.registerComponentInstance("fruit", "apple");
        recorded.registerComponentInstance("int", new Integer(239));
        recorded.registerComponentImplementation("thing",
                ThingThatTakesParamsInConstructor.class,
                new Parameter[]{
                    ComponentParameter.DEFAULT,
                    ComponentParameter.DEFAULT,
                });

        MutablePicoContainer slave = new DefaultPicoContainer();
        recorder.replay(slave);
        assertEquals("apple", slave.getComponentInstance("fruit"));
        assertEquals("apple239", ((ThingThatTakesParamsInConstructor) slave.getComponentInstance("thing")).getValue());

        // test that we can replay once more
        MutablePicoContainer anotherSlave = new DefaultPicoContainer();
        recorder.replay(anotherSlave);
        assertEquals("apple", anotherSlave.getComponentInstance("fruit"));
        assertEquals("apple239", ((ThingThatTakesParamsInConstructor) anotherSlave.getComponentInstance("thing")).getValue());
    }

    public void testRecorderWorksAfterSerialization() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        ContainerRecorder recorder = new DefaultContainerRecorder(new DefaultPicoContainer());
        MutablePicoContainer recorded = recorder.getContainerProxy();
        recorded.registerComponentInstance("fruit", "apple");

        ContainerRecorder serializedRecorder = (ContainerRecorder) serializeAndDeserialize(recorder);
        MutablePicoContainer slave = new DefaultPicoContainer();
        serializedRecorder.replay(slave);
        assertEquals("apple", slave.getComponentInstance("fruit"));
    }

    private Object serializeAndDeserialize(Object o) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(o);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

        return ois.readObject();
    }

    // Fails to find class classloader (simpler than one below)
    public void DONOT_testXMLRecorderHierarchyWorksWithoutRecorder() throws ClassNotFoundException {

        //System.out.println("--> 1 " + Thread.currentThread().getContextClassLoader());
        //System.out.println("--> 2 " + WilmaImpl.class.getClassLoader());

        MutablePicoContainer parentPrototype = new DefaultPicoContainer();
        StringReader parentResource = new StringReader(""
                + "<container>"
                + "  <component-implementation key='wilma' class='"+WilmaImpl.class+"'/>"
                + "</container>"
                );

        XMLContainerBuilder builder = new XMLContainerBuilder(parentResource, WilmaImpl.class.getClassLoader());
        builder.populateContainer(parentPrototype);
        assertNotNull(parentPrototype.getComponentInstances().get(0));
    }


    // Fails to find class classloader
    public void DONOT_testXMLRecorderHierarchy() throws ClassNotFoundException {

        //System.out.println("--> 1 " + Thread.currentThread().getContextClassLoader());
        //System.out.println("--> 2 " + WilmaImpl.class.getClassLoader());

        MutablePicoContainer parentPrototype = new DefaultPicoContainer();
        DefaultContainerRecorder parentRecorder = new DefaultContainerRecorder(parentPrototype);
        StringReader parentResource = new StringReader("" 
                + "<container>" 
                + "  <component-implementation key='wilma' class='"+WilmaImpl.class+"'/>"  
                + "</container>" 
                );

        populateXMLContainer(parentRecorder, parentResource);

        MutablePicoContainer childPrototype = new DefaultPicoContainer(parentPrototype);
        DefaultContainerRecorder childRecorder = new DefaultContainerRecorder(childPrototype);
        StringReader childResource = new StringReader("" 
                + "<container>" 
                + "  <component-implementation key='fred' class='"+FredImpl.class+"'>"  
                + "     <parameter key='wilma'/>"  
               + "  </component-implementation>"  
                + "</container>" 
                );
        populateXMLContainer(childRecorder, childResource);
        
    }
    
    private void populateXMLContainer(ContainerRecorder recorder, Reader resource) {
        ContainerPopulator populator = new XMLContainerBuilder(resource, Thread.currentThread().getContextClassLoader());
        populator.populateContainer(recorder.getContainerProxy());
    }       
}
