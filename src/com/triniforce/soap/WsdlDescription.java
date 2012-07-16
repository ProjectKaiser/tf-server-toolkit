/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.soap.TypeDef;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.EnumDef;
import com.triniforce.soap.TypeDef.MapDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.WsdlDescription.WsdlPort.WsdlOperation;

public class WsdlDescription {

    private InterfaceDescription m_iDesc;

    public WsdlDescription(InterfaceDescription iDesc) {
        m_iDesc = iDesc;
    }
    
    // wsdl methods
    static class WsdlTypeElement{
        private String m_name;
        private TypeDef m_td;
        private boolean m_bResident;
        private int m_maxOccur;
        public WsdlTypeElement(String name, TypeDef td, boolean bResident, int maxOccur) {
            m_name = name;
            m_td  = td;
            m_bResident = bResident;
            m_maxOccur = maxOccur;
        }
        String  getName() {
            return m_name;
        }
        WsdlType getType() {
            return new WsdlType(m_td);
        }
        
        int getMinOccur(){
            return m_td.isNullable() ? 0 : 1;
        }
        int getMaxOccur(){
            return m_maxOccur;
        }
        boolean isResidentType(){
            return m_bResident;
        }
    }
    
    static class WsdlSequence extends AbstractCollection<WsdlTypeElement>{
        
        private List<PropDef> m_props;

        public WsdlSequence(List<PropDef> props) {
            m_props = props;
        }

        @Override
        public Iterator<WsdlTypeElement> iterator() {
            return new Iterator<WsdlTypeElement>(){
                Iterator<PropDef> m_i = m_props.iterator();

                public boolean hasNext() {
                    return m_i.hasNext();
                }

                public WsdlTypeElement next() {
                    PropDef v = m_i.next();
                    return new WsdlTypeElement(v.getName(), v.getType(), true, 1);
                }

                public void remove() {}
                
            };
        
        }

        @Override
        public int size() {
            return m_props.size();
        }
        
    }
    
    static final HashSet<Type> PRIMITIVES = new HashSet<Type>(
        Arrays.asList((Type)Boolean.TYPE, Integer.TYPE, Long.TYPE,
                Short.TYPE, Float.TYPE, Double.TYPE )        
    );
    
    static class WsdlType{
        private TypeDef m_td;
        public WsdlType(TypeDef td) {
            m_td = td;
        }
        Collection<WsdlTypeElement> getElements() {
            if(m_td instanceof ClassDef){
                ClassDef cd = (ClassDef)m_td ;
                return new WsdlSequence(cd.getOwnProps());
            }
            else if(m_td instanceof ArrayDef){
                ArrayDef ad = (ArrayDef) m_td;
                return Arrays.asList(new WsdlTypeElement("value", ad.getComponentType(), 
                		!(m_td instanceof MapDef) , -1));
            }
            else{
                return Collections.emptyList();
            }
        }    
        
        public TypeDef getTypeDef() {
            return m_td;
        }
        
        String getParentName(){
            if(!(m_td instanceof ClassDef))
                return null;
            ClassDef cd = (ClassDef) m_td;
            return null == cd.getParentDef() ? null : cd.getParentDef().getName();
        }
        
        List<String> getResriction(){
        	if(!(m_td instanceof EnumDef)){
        		return null;
        	}
        	EnumDef ed = (EnumDef) m_td;
        	return Arrays.asList(ed.getPossibleValues());
        }
        
        boolean isComplex(){
        	return !(m_td instanceof ScalarDef);
        }

        
    }
    
    Collection<WsdlTypeElement> getWsdlTypeElements(){
        return new WsdlSequence(new AbstractList<PropDef>(){
            @Override
            public PropDef get(int arg0) {
                Operation op = m_iDesc.getOperations().get(arg0/2);
                return op.getProps().get(arg0 & 1);
            }

            @Override
            public int size() {
                return m_iDesc.getOperations().size()*2;
            }
        });
    }
    
    Collection<WsdlType> wsdlTypes(final Collection<? extends TypeDef> from){
        return new AbstractCollection<WsdlType>(){
            @Override
            public int size() {
                return from.size();
            }
            @Override
            public Iterator<WsdlType> iterator() {
                return new Iterator<WsdlType>(){
                    Iterator<? extends TypeDef> m_i = from.iterator();
                    public boolean hasNext() {
                        return m_i.hasNext();
                    }

                    public WsdlType next() {
                        return new WsdlType(m_i.next());
                    }

                    public void remove() {}
                    
                };
            }
        };
        
    }
    
    public Collection<WsdlType> getWsdlTypes() {
        return wsdlTypes(m_iDesc.getTypes());
    }
    
    interface WsdlPort{
        interface WsdlOperation{
            String getName();
            WsdlMessage getInput();
            WsdlMessage getOutput();
        }
        String getName();
        List<WsdlOperation> getOperations();
    }
    
    interface WsdlMessage{
        String getMessageName();
        String getElementName();
    }
    
    WsdlPort getWsdlPort(){
        return new WsdlPort(){

            public String getName() {
                return null;//m_serviceName + "Soap";
            }

            public List<WsdlOperation> getOperations() {
                return new AbstractList<WsdlOperation>(){

                    @Override
                    public WsdlOperation get(final int arg0) {
                        return new WsdlOperation(){
                            Operation op = m_iDesc.getOperations().get(arg0);

                            public String getName() {
                                return op.getName();
                            }
                            public WsdlMessage getInput() {
                                return new WsdlMessage(){
                                    public String getElementName() {
                                        return op.getName();
                                    }
                                    public String getMessageName() {
                                        return op.getName()+"SoapIn";
                                    }
                                };
                            }
                            public WsdlMessage getOutput() {
                                return new WsdlMessage(){
                                    public String getElementName() {
                                        return op.getName()+"Response";
                                    }
                                    public String getMessageName() {
                                        return op.getName()+"SoapOut";
                                    }
                                };
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return m_iDesc.getOperations().size();
                    }
                };
            }
            
        };
    }
    
    List<WsdlMessage> getMessages(){
        return new AbstractList<WsdlMessage>(){
            List<WsdlOperation> m_ops = getWsdlPort().getOperations();
            @Override
            public WsdlMessage get(int arg0) {
                WsdlOperation op = m_ops.get(arg0/2);
                return (arg0&1) == 0 ? op.getInput() : op.getOutput();
            }

            @Override
            public int size() {
                return m_ops.size()*2;
            }
            
        };
    }
}
