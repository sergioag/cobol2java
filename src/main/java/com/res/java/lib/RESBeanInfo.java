package com.res.java.lib;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

public class RESBeanInfo implements BeanInfo {

	@Override
	public BeanInfo[] getAdditionalBeanInfo() {
		return null;
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return null;
	}

	@Override
	public int getDefaultEventIndex() {
		return 0;
	}

	@Override
	public int getDefaultPropertyIndex() {
		return 0;
	}

	@Override
	public EventSetDescriptor[] getEventSetDescriptors() {
		return null;
	}

	@Override
	public Image getIcon(int iconKind) {
		return null;
	}

	@Override
	public MethodDescriptor[] getMethodDescriptors() {
		return null;
	}

	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return null;
	}

}
