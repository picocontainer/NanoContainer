package org.nanocontainer.reflection;

import junit.framework.TestCase;
import org.nanocontainer.integrationkit.ContainerRecorder;
import org.nanocontainer.testmodel.ThingThatTakesParamsInConstructor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.defaults.ComponentParameter;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

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
}
