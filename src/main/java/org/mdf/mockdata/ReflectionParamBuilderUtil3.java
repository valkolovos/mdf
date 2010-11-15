package org.mdf.mockdata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.reflection.MockDataReader;
import org.mdf.mockdata.reflection.MockDataXmlDriver;

import com.google.protobuf.ByteString;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StreamException;

public class ReflectionParamBuilderUtil3 {

    public static final String IMPLEMENTATION_TYPE_PARAM_NAME = "Implementation Type";
    public static final String SERIALIZE_DATA = "serialize-data";
    public static final String XSTREAM_ATTRIBUTE = "XStream Attribute: ";
    private static GenericObjectPool xstreamWorkerPool;

    static {
        xstreamWorkerPool = new GenericObjectPool(new XStreamWorkerPoolFactory(), -1,
                GenericObjectPool.WHEN_EXHAUSTED_GROW, 0, -1, false, false, 60000, 10, 30000, false);
    }

    private static class XStreamWorkerPoolFactory implements PoolableObjectFactory {

        public void activateObject(Object object) throws Exception {
        }

        public void destroyObject(Object object) throws Exception {
        }

        public Object makeObject() throws Exception {
            MockDataXmlDriver hierarchicalStreamDriver = new MockDataXmlDriver();
            RPBUXStream xstream = new RPBUXStream(hierarchicalStreamDriver);
            xstream.registerConverter(new DateTimeZoneConverter());
            xstream.registerConverter(new ProtbufMessageConverter());
            return xstream;
        }

        public void passivateObject(Object object) throws Exception {
        }

        public boolean validateObject(Object object) {
            return true;
        }

    }

    public static void buildParamFromObject(Object o, Param p) throws Exception {
        buildParamFromObject(o, p, true);
    }

    public static void buildParamFromObject(Object o, Param p, boolean useReferences) throws Exception {
        RPBUXStream xstream = (RPBUXStream) xstreamWorkerPool.borrowObject();
        try {
            if (useReferences) {
                xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
            } else {
                xstream.setMode(XStream.NO_REFERENCES);
            }
            xstream.toXML(o);
            p.setValue(xstream._driver.getWriter().getParam().getValue());
            p.setParam(xstream._driver.getWriter().getParam().getParam());
            if (o != null && !isPrimitiveType(o.getClass())) {
                Param implementationTypeParam = new Param();
                implementationTypeParam.setName(IMPLEMENTATION_TYPE_PARAM_NAME);
                implementationTypeParam.setValue(xstream._driver.getWriter().getParam().getName());
                p.addParam(implementationTypeParam);
            }
        } finally {
            xstreamWorkerPool.returnObject(xstream);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T buildObjectFromParams(Param responseParam, Class<T> responseClass)
            throws Exception {
        if (responseParam.getParamCount() == 0 && responseParam.getValue() == null) {
            return null;
        }
        Param clonedParam = new Param();
        clonedParam.setName(responseParam.getName());
        clonedParam.setValue(responseParam.getValue());
        boolean typeSet = false;
        List<Param> newChildParams = new ArrayList<Param>();
        for (Param param : responseParam.getParam()) {
            if (param.getName().equals(IMPLEMENTATION_TYPE_PARAM_NAME)) {
                clonedParam.setName(param.getValue());
                typeSet = true;
            } else {
                Param p = new Param();
                p.setName(param.getName());
                p.setValue(param.getValue());
                p.setParam(param.getParam());
                newChildParams.add(p);
            }
        }
        clonedParam.setParam(newChildParams.toArray(new Param[newChildParams.size()]));
        if (!typeSet) {
            clonedParam.setName(responseClass.getName());
        }
        RPBUXStream xstream = (RPBUXStream) xstreamWorkerPool.borrowObject();
        xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES); // if references are used, this will make sure they are understood. if not, no harm done.
        T obj = null;
        try {
            obj = (T) xstream.fromXML(new MockDataReader(clonedParam));
        } finally {
            xstreamWorkerPool.returnObject(xstream);
        }
        return obj;
    }
    
    public static boolean isPrimitiveType(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)
                || Character.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)
                || Void.class.isAssignableFrom(clazz);
    }

    private static class DateTimeZoneConverter implements Converter {

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            writer.startNode("tzId");
            writer.setValue(((DateTimeZone) source).getID());
            writer.endNode();
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            reader.moveDown();
            String tzId = reader.getValue();
            reader.moveUp();
            if (tzId.equals("UTC")) {
                return DateTimeZone.UTC;
            } else {
                return DateTimeZone.forID(tzId);
            }
        }

        @SuppressWarnings("unchecked")
        public boolean canConvert(Class type) {
            return DateTimeZone.class.isAssignableFrom(type);
        }

    }

    private static class ProtbufMessageConverter implements Converter {

        @SuppressWarnings("unchecked")
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            Message message = (Message) source;
            try {
                for (Map.Entry<FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
                    FieldDescriptor descrip = entry.getKey();
                    Object object = entry.getValue();
                    if (descrip.isRepeated()) {
                        Collection c = (Collection) object;
                        for (Object obj : c) {
                            buildValue(descrip, obj, writer, context);
                        }
                    } else {
                        buildValue(descrip, object, writer, context);
                    }
                }
            } catch (Exception e) {
                throw new StreamException(e);
            }
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            try {
                String className = reader.getNodeName();
                Class<?> responseClass = Class.forName(className);
                Method builderMethod = responseClass.getMethod("newBuilder");
                Builder<?> builder = (Builder<?>) builderMethod.invoke(null);
                Map<String, FieldDescriptor> descriptorMap = new HashMap<String, FieldDescriptor>();
                for (FieldDescriptor desc : builder.getDescriptorForType().getFields()) {
                    descriptorMap.put(desc.getName(), desc);
                }
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    FieldDescriptor descriptor = descriptorMap.get(reader.getNodeName());
                    if (descriptor == null) {
                        Logger.getLogger(getClass()).warn("No field descriptor found for " + reader.getNodeName());
                        continue;
                    }
                    if (descriptor.isRepeated()) {
                        builder.addRepeatedField(descriptor, cast(descriptor, reader));
                    } else {
                        builder.setField(descriptor, cast(descriptor, reader));
                    }
                    reader.moveUp();
                }
                return builder.build();
            } catch (Exception e) {
                throw new StreamException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public boolean canConvert(Class type) {
            return Message.class.isAssignableFrom(type);
        }

        private void buildValue(FieldDescriptor descrip, Object source, HierarchicalStreamWriter writer,
                MarshallingContext context) throws Exception {
            writer.startNode(descrip.getName());
            if (descrip.getJavaType() == JavaType.BYTE_STRING) {
                writer.setValue(((ByteString) source).toStringUtf8());
            } else if (descrip.getJavaType() == JavaType.ENUM) {
                writer.setValue(((EnumValueDescriptor) source).getName());
            } else if (descrip.getJavaType() == JavaType.MESSAGE) {
                context.convertAnother(source);
            } else {
                writer.setValue(source.toString());
            }
            writer.endNode();
        }

        private Object buildObject(Builder<?> builder, HierarchicalStreamReader reader) {
            Map<String, FieldDescriptor> descriptorMap = new HashMap<String, FieldDescriptor>();
            for (FieldDescriptor desc : builder.getDescriptorForType().getFields()) {
                descriptorMap.put(desc.getName(), desc);
            }
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                FieldDescriptor descriptor = descriptorMap.get(reader.getNodeName());
                if (descriptor == null) {
                    Logger.getLogger(getClass()).warn("No field descriptor found for " + reader.getNodeName());
                    continue;
                }
                if (descriptor.isRepeated()) {
                    builder.addRepeatedField(descriptor, cast(descriptor, reader));
                } else {
                    builder.setField(descriptor, cast(descriptor, reader));
                }
                reader.moveUp();
            }
            return builder.build();
        }

        private Object cast(FieldDescriptor descriptor, HierarchicalStreamReader reader) {
            if (descriptor.getJavaType() == JavaType.STRING) {
                return reader.getValue();
            } else if (descriptor.getJavaType() == JavaType.INT) {
                return Integer.parseInt(reader.getValue());
            } else if (descriptor.getJavaType() == JavaType.BOOLEAN) {
                return Boolean.parseBoolean(reader.getValue());
            } else if (descriptor.getJavaType() == JavaType.DOUBLE) {
                return Double.parseDouble(reader.getValue());
            } else if (descriptor.getJavaType() == JavaType.LONG) {
                return Long.parseLong(reader.getValue());
            } else if (descriptor.getJavaType() == JavaType.FLOAT) {
                return Float.parseFloat(reader.getValue());
            } else if (descriptor.getJavaType() == JavaType.BYTE_STRING) {
                return ByteString.copyFromUtf8(reader.getValue());
            } else if (descriptor.getJavaType() == JavaType.ENUM) {
                for (EnumValueDescriptor evalDesc : descriptor.getEnumType().getValues()) {
                    if (evalDesc.getName().equals(reader.getValue())) {
                        return evalDesc;
                    }
                }
            } else if (descriptor.getJavaType() == JavaType.MESSAGE) {
                Builder<?> b = DynamicMessage.newBuilder(descriptor.getMessageType());
                Object o = buildObject(b, reader);
                return o;
            }
            return null;
        }

    }

    private static class RPBUXStream extends XStream {
        private MockDataXmlDriver _driver;

        public RPBUXStream(MockDataXmlDriver driver) {
            super(driver);
            _driver = driver;
        }
    }

}
